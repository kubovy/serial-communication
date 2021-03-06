/******************************************************************************
 * Copyright (C) 2020 Jan Kubovy (jan@kubovy.eu)                              *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published      *
 * by the Free Software Foundation, either version 3 of the License, or (at   *
 * your option) any later version.                                            *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU Lesser General Public License for more details.                        *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this program.  If not, see                              *
 * <http://www.gnu.org/licenses/>.                                            *
 ******************************************************************************/
package com.poterion.communication.serial.communicator

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * USB communicator.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class USBCommunicator : Communicator<USBCommunicator.Descriptor>(Channel.USB) {

	/**
	 * USB connection descriptor.
	 *
	 * @param portName Port name
	 */
	data class Descriptor(val portName: String) {
		override fun toString(): String = portName
	}

	private enum class State {
		IDLE,
		LENGTH_HIGH,
		LENGTH_LOW,
		ADDITIONAL
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(USBCommunicator::class.java)
		const val MAX_PACKET_SIZE = 35 // TODO
	}

	private var serialPort: SerialPort? = null
	private var messageBuffer: Queue<ByteArray> = ConcurrentLinkedQueue()

	private val serialPortEventListener = object : SerialPortEventListener {
		private val buffer: ByteArray = ByteArray(MAX_PACKET_SIZE)
		private var chksum = 0
		private var index = 0
		private var length = 0
		private var state = State.IDLE

		override fun serialEvent(event: SerialPortEvent) {
			if (event.isRXCHAR) { // If data is available
				val (length, data) = event.eventValue
						.takeIf { it > 0 }
						?.let { it to serialPort?.readBytes(it) }
						?.takeIf { (length, data) -> data?.size == length }
						?: 0 to null

				if (data != null && data.size == length) {
					data.forEach { byte ->
						when (state) {
							State.IDLE -> {
								if (byte.toUInt() == 0xAA) {
									this.state =
										State.LENGTH_HIGH
									this.chksum = 0
									//LOGGER.debug("USB> 0xAA: IDLE -> LENGTH_HIGH")
								}
							}
							State.LENGTH_HIGH -> {
								this.length = byte.toUInt() * 256
								this.chksum += byte.toUInt()
								this.state =
									State.LENGTH_LOW
								//LOGGER.debug("USB> 0x%02X: LENGTH_HIGH -> LENGTH_LOW".format(byte))
							}
							State.LENGTH_LOW -> {
								this.length += byte.toUInt()
								if (this.length > buffer.size) {
									this.state =
										State.IDLE
									//LOGGER.debug("USB> 0x%02X: LENGTH_LOW -> IDLE".format(byte))
								} else {
									this.chksum += byte.toUInt()
									this.index = 0
									this.state =
										State.ADDITIONAL
									//LOGGER.debug("USB> 0x%02X: LENGTH_LOW -> ADDITIONAL".format(byte))
								}
							}
							State.ADDITIONAL -> {
								if (this.index < this.length) {
									this.buffer[this.index++] = byte
									this.chksum += byte.toUInt()
								} else {
									this.chksum = (0xFF - this.chksum + 1) and 0xFF
									if (this.chksum == byte.toUInt()) {
										//LOGGER.debug("USB> 0x%02X: checksum OK (0x%02X)".format(byte, this.chksum))
										messageBuffer.add(buffer.sliceArray(0 until this.length))
									} else {
										LOGGER.warn("${logTag} 0x%02X: Wrong checksum (0x%02X)".format(byte, this.chksum))
									}
									this.state =
										State.IDLE
								}
							}
						}
					}
				}
			}
		}
	}

	override fun canConnect(descriptor: Descriptor): Boolean =
			descriptor.portName.isNotEmpty() && super.canConnect(descriptor)

	override fun createConnection(): Boolean = connectionDescriptor
			?.portName
			?.let { SerialPort(it) }
			?.also {
				try {
					serialPort = it
					it.openPort()
					it.setParams(SerialPort.BAUDRATE_115200,
							SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE)
					it.addEventListener(serialPortEventListener)
				} catch (e: SerialPortException) {
					LOGGER.error("${logTag} ${e.message}")
				}
			} != null

	override fun cleanUpConnection() {
		serialPort?.takeIf { it.isOpened }?.closePort()
		serialPort = null
	}

	override fun nextMessage(): ByteArray? = messageBuffer.poll()

	override fun sendMessage(data: ByteArray) {
		serialPort?.writeBytes(data.wrap())
	}

	/**
	 * This implements a lower communication layer similar to the IS167x.
	 *
	 * One application packet can be divided into multiple interface packets.
	 *
	 *     |================================|
	 *     |             PACKET             |
	 *     |--------------------------------|
	 *     |SYNC|LENH|LENL|DATA        |CRC |
	 *     |--------------------------------|
	 *     |0xAA|0xXX|0xXX|0xXX .. 0xXX|0xXX|
	 *     |--------------------------------|
	 *     |    |         |---- LEN ---|    |
	 *     |    |-------- CRC -------- |    |
	 *     |--------------------------------|
	 *     |================================|
	 */
	private fun ByteArray.wrap() = ByteArray(size + 4) {
		when (it) {
			0 -> 0xAA.toByte()
			1 -> (size / 256.0).toInt().toByte()
			2 -> (size % 256.0).toInt().toByte()
			size + 3 -> (0xFF - ((size / 256.0).toInt() + (size % 256.0).toInt() + reduce { acc, byte -> (acc + byte).toByte() }) + 1).toByte()
			else -> get(it - 3)
		}
	}

	@Suppress("EXPERIMENTAL_API_USAGE")
	private fun Byte.toUInt() = toUByte().toInt()
}