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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.microedition.io.Connector
import javax.microedition.io.StreamConnection

/**
 * Bluetooth communicator.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class BluetoothCommunicator : Communicator<BluetoothCommunicator.Descriptor>(Channel.BLUETOOTH) {

	/**
	 * Bluetooth connection descriptor.
	 *
	 * @param address Bluetooth address
	 * @param channel Channel
	 */
	data class Descriptor(val address: String, val channel: Int, var name: String? = null) {
		override fun toString(): String = "${address}[${channel}]"
	}

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(BluetoothCommunicator::class.java)
	}

	private val url: String?
		get() = connectionDescriptor?.let { "btspp://${it.address.replace(":", "")}:${it.channel};authenticate=false;encrypt=false;master=false" }

	private var streamConnection: StreamConnection? = null
	private var outputStream: OutputStream? = null
	private var inputStream: InputStream? = null

	override fun canConnect(descriptor: Descriptor) =
			descriptor.address.isNotEmpty()
					&& descriptor.address.matches("[0-9A-Fa-f]{2}(:[0-9A-Fa-f]{2}){5}".toRegex())
					&& super.canConnect(descriptor)

	override fun createConnection(): Boolean = url
			?.let {
				try {
					Connector.open(url) as StreamConnection
				} catch (e: IOException) {
					LOGGER.error("${logTag} ${e.message}")
					null
				}
			}
			?.also {
				//val uuid = UUID("1101", true) // Create a UUID for SPP (1101)
				streamConnection = it
				//Wait for client connection
				LOGGER.debug("${logTag} Server Started. Waiting for clients to connect...")

				//val connection = streamConnNotifier!!.acceptAndOpen()
				//println("Remote device address: " + RemoteDevice.getRemoteDevice(connection).bluetoothAddress)
				//println("Remote device name: " + RemoteDevice.getRemoteDevice(connection).getFriendlyName(true))

				//the stream is opened both in and out
				outputStream = streamConnection?.openOutputStream()
				inputStream = streamConnection?.openInputStream()
			} != null

	override fun cleanUpConnection() {
		inputStream?.close()
		inputStream = null
		outputStream?.close()
		outputStream = null
		streamConnection?.close()
		streamConnection = null
	}

	override fun nextMessage(): ByteArray? {
		val buffer = ByteArray(256)
		val length = inputStream?.read(buffer) ?: 0
		return buffer.takeIf { length > 0 }?.copyOfRange(0, length)
	}

	override fun sendMessage(data: ByteArray) {
		outputStream?.write(data)
		outputStream?.flush()
	}
}