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
@file:Suppress("MemberVisibilityCanBePrivate")
package com.poterion.communication.serial.communicator

import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.byte2Bools
import com.poterion.communication.serial.calculateChecksum
import com.poterion.communication.serial.communicator.Communicator.Companion.MAX_SEND_ATTEMPTS
import com.poterion.communication.serial.communicator.Communicator.Companion.MESSAGE_CONFIRMATION_TIMEOUT
import com.poterion.communication.serial.payload.DeviceCapabilities
import com.poterion.communication.serial.payload.DeviceIdState
import com.poterion.communication.serial.toByteArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.microedition.io.ConnectionNotFoundException
import kotlin.random.Random

/**
 * Abstract serial communicator enabling to communicated with a peripheral device over a [Channel] using binary
 * messages. The first 2 bytes of every message have a fixed semantic:
 *
 *      | CRC|KIND|...
 *      |0xXX|0xYY|...
 *
 * First byte is a [checksum][calculateChecksum] and the second byte is a [MessageKind]. The rest of the message
 * depends on the implementation of a concrete [MessageKind]. The maximum length of a message is determined by the
 * [Channel.maxPacketSize]. If necessary a message needs to be split to different packets.
 *
 * A communicator manages three threads: connection thread, inbound thread, and outbound thread.
 *
 * The connection thread in [State.CONNECTING] interrupts both, inbound and outboud, communication threads, if such
 * are alive. Clean up the connection and tries to start new set of inbound and outbound communication threads. In all
 * other [states][State] it just sleeps.
 *
 * The inbound thread waits for [nextMessage], which is expected to be implemented as a blocking function. Only non
 * empty messages are considered.
 *
 * First byte of that message is extracted as a message-checksum calculated by the
 * sender. Then the rest of the message is used to [calculate a checksum][calculateChecksum] on the receiver side.
 * If both, the received and the calculated, checksums match, the message is considered as valid for further processing.
 * Otherwise, the message is discarded.
 *
 * A [MessageKind] is then determined based on the second byte. All non-[CRC][MessageKind.CRC] messages a CRC checksum
 * is send back to the sender containing the [calculated checksum][calculateChecksum]:
 *
 *       | CRC|KIND|CONTENT|
 *       |0xXX|0x00|  0xXX |
 *
 * A [MessageKind.CRC] message will store the received CRC to compare with the last sent message. This way the
 * communicator determines when a sent message was successfully received by the other side.
 *
 * A [MessageKind.IDD] message implements a protocol to initialize a connection and determine its stability.
 *
 * Any other [MessageKind] will be simply forwarded using the
 * [com.poterion.communication.serial.listeners.CommunicatorListener.onMessageReceived] and has to be implemented
 * further.
 *
 * Last, the outbound thread monitors 2 queues: the _checksum queue_ and the _message queue_. The _checksum queue_
 * has priority over the _message queue_. The _checksum queue_ contains a list of [checksum bytes][calculateChecksum]
 * of recently received messages to be confirmed to the sender. The _message queue_ contains a list of binary messages
 * to be send to the receiver without the checksum byte (the 1st byte). The checksum will be calculated freshly before
 * any send attempt.
 *
 * The following flows and situactions are considered during communication between the sender and the receiver. Both
 * side share this implementation:
 *
 * In good case transmitting a message works first time and is being confirmed right-away:
 *
 * ![Success](https://github.com/kubovy/serial-communication/raw/master/src/img/communication-success.png)
 *
 * One typical error case is when a message does not reach its target. In this case the message is resend again after
 * a certain timeout (by default 500ms - [MESSAGE_CONFIRMATION_TIMEOUT], but can be overrwritten for different
 * [MessageKind] - [MessageKind.delay]).
 *
 * ![Sent timeout](https://github.com/kubovy/serial-communication/raw/master/src/img/communication-sent-timeout.png)
 *
 * In the same way it may happen, that the message got received corretly, but the CRC was not. Each [MessageKind]
 * needs to be implemented in a idempotent way so receiving multiple same messages after each other should not result
 * in errors.
 *
 * ![CRC timeout](https://github.com/kubovy/serial-communication/raw/master/src/img/communication-confirmation-timeout.png)
 *
 * To prevent resent starvation, one message is retried up to 20 times by default ([MAX_SEND_ATTEMPTS]) and then it
 * is dropped. The developer is responsible for resolution of such situations.
 *
 * The connection can be in one of the following states:
 *
 *  - [State.DISCONNECTED]
 *  - [State.CONNECTING]
 *  - [State.CONNECTED]
 *  - [State.DISCONNECTING]
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 * @param ConnectionDescriptor Implementation specific connection description.
 * @param channel [Channel] to use.
 * @see com.poterion.communication.serial.listeners.CommunicatorListener
 * @see MessageKind
 * @see Channel
 * @see State
 */
/*
 * @startuml
 * participant Sender as A
 * participant Receiver as B
 *
 * autonumber "<i>[#]</i>"
 * A   ->  B : Message
 * A  <--  B : CRC
 * @enduml
 */
/*
 * @startuml
 * participant Sender as A
 * participant Receiver as B
 *
 * autonumber "<i>[#]</i>"
 * A   ->x B: Message
 * ... timeout ...
 * A   ->  B: Message
 * A  <--  B: CRC
 * @enduml
 */
/*
 * @startuml
 * participant Sender as A
 * participant Receiver as B
 *
 * autonumber "<i>[#]</i>"
 * A   ->  B: Message
 * A x<--  B: CRC
 * ... timeout ...
 * A   ->  B: Message
 * A  <--  B: CRC
 * @enduml
 */
abstract class Communicator<ConnectionDescriptor>(internal val channel: Channel):
	CommunicatorBase<ConnectionDescriptor>() {

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(Communicator::class.java)
		const val CRC_PRINT = false
		const val IDD_PING = false
		const val IDD_PING_PRINT = false
		const val MESSAGE_CONFIRMATION_TIMEOUT = 500L // default delay in ms
		const val MAX_SEND_ATTEMPTS = 20
	}

//	/** Whether the device is in [State.CONNECTED] or not */
//	val isConnected: Boolean
//		get() = state == State.CONNECTED
//
//	/** Whether the device is in [State.CONNECTING] or not */
//	val isConnecting: Boolean
//		get() = state == State.CONNECTING
//
//	/** Whether the device is in [State.DISCONNECTING] or not */
//	val isDisconnecting: Boolean
//		get() = state == State.DISCONNECTING
//
//	/** Whether the device is in [State.DISCONNECTED] or not */
//	val isDisconnected: Boolean
//		get() = state == State.DISCONNECTED

	private var connectionRequested = false

	/** Current state of the connection to the device. */
	final override var state = State.DISCONNECTED
		private set

	final override var deviceCapabilities: DeviceCapabilities = DeviceCapabilities()
		private set

	final override var deviceName: String = ""
		private set

	private var iddState = 0x00
	private var iddCounter = 0
	private var attempt = 0

	private val messageQueue: ConcurrentLinkedQueue<Pair<ByteArray, Long?>> = ConcurrentLinkedQueue()
	private val checksumQueue: ConcurrentLinkedQueue<Byte> = ConcurrentLinkedQueue()
	private var lastChecksum: Int? = null

	private val connectorExecutor = Executors.newSingleThreadExecutor()
	private val inboundExecutor = Executors.newSingleThreadExecutor()
	private val outboundExecutor = Executors.newSingleThreadExecutor()

	private var connectorThread: Thread? = null
	private var inboundThread: Thread? = null
	private var outboundThread: Thread? = null

	private var idleLoops = 0
	private var onDemand = false
	protected val logTag: String
		get() = "${channel} ${connectionDescriptor}>"

	private val connectorRunnable: () -> Unit = {
		Thread.currentThread().name = "${channel} Connector"
		while (!Thread.interrupted()) {
			if (state == State.CONNECTING) {
				LOGGER.debug("${logTag} Connection attempt ...")

				inboundThread?.takeIf { it.isAlive }?.interrupt()
				outboundThread?.takeIf { it.isAlive }?.interrupt()
				cleanUpConnection()

				while (state == State.CONNECTING) try {
					if (createConnection()) {
						iddState = if (onDemand) DeviceIdState.values().size else 0x00
						iddCounter = 0
						attempt = 0

						inboundThread?.takeIf { !it.isInterrupted }?.interrupt()
						outboundThread?.takeIf { !it.isInterrupted }?.interrupt()

						inboundThread = Thread(inboundRunnable)
						inboundThread?.name = "${channel}-inbound"
						outboundThread = Thread(outboundRunnable)
						outboundThread?.name = "${channel}-outbound"

						inboundExecutor.execute(inboundThread!!)
						outboundExecutor.execute(outboundThread!!)

						state = State.CONNECTED
						listeners.forEach { it.onConnect(channel) }
					}
					Thread.sleep(1000)
				} catch (e: Exception) {
					LOGGER.error("${logTag} ${e.message}", e)
					Thread.sleep(1000)
					disconnectInternal(stayDisconnected = false)
					onDemand = false
				}
			} else if (state == State.CONNECTED) {
				Thread.sleep(100)
				idleLoops++
				if (!onDemand && idleLoops >= 30) {
					if (messageQueue.isEmpty() && checksumQueue.isEmpty()) sendPing()
					idleLoops = 0
				}
			} else {
				Thread.sleep(100)
			}
		}
		LOGGER.debug("${logTag} Connection thread exited")
	}

	private val inboundRunnable: () -> Unit = {
		Thread.currentThread().name = "${channel} Inbound"
		try {
			while (!Thread.interrupted() && state == State.CONNECTED) try {
				val message = nextMessage()
				val data = message?.map { it.toInt() }
				idleLoops = 0

				if (message != null && data != null) {
					val chksumReceived = message[0].toInt() and 0xFF
					val chksumCalculated = message.toList().subList(1, message.size).toByteArray().calculateChecksum()
					message.toDebugMessage("Inbound ", chksumReceived)?.also { LOGGER.debug(it) }
					if (chksumCalculated == chksumReceived) {
						val messageKind = message[1]
							.let { byte -> MessageKind.values().find { it.code.toByte() == byte } }
							?: MessageKind.UNKNOWN

						if (messageKind != MessageKind.CRC) checksumQueue.add(chksumCalculated.toByte())

						when (messageKind) {
							MessageKind.CRC -> {
								lastChecksum = (message[2].toInt() and 0xFF)
								//LOGGER.debug("${logTag}" +
								//		" Inbound [CRC] ${"0x%02X".format(lastChecksum)}")
							}
							MessageKind.IDD -> {
								if (message.size > 3) {
									iddState = message[3].toUInt() + 1
									when (message[3].toUInt()) {
										0x00 -> {
											deviceCapabilities = message
												.takeIf { it.size == 6 }
												?.copyOfRange(4, 6)
												?.map { it.toUInt() }
												?.let { byte2Bools(it[0]) + byte2Bools(it[1]) }
												?.let {
													DeviceCapabilities(
														hasBluetooth = it[0],
														hasUSB = it[1],
														hasTemp = it[2],
														hasLCD = it[3],
														hasRegistry = it[4],
														hasMotionSensor = it[5],
														// it[06],
														// it[07]
														hasRgbStrip = it[8],
														hasRgbIndicators = it[9],
														hasRgbLight = it[10]
														// it[11],
														// it[12],
														// it[13],
														// it[14],
														// it[15]
													)
												} ?: DeviceCapabilities()
											listeners.forEach {
												it.onDeviceCapabilitiesChanged(channel, deviceCapabilities)
											}
										}
										0x01 -> {
											deviceName = message
												.toList()
												.subList(4, message.size)
												.toByteArray()
												.toString(Charsets.UTF_8)
											listeners.forEach { it.onDeviceNameChanged(channel, deviceName) }
											listeners.forEach { it.onConnectionReady(channel) }
										}
									}
								}
								message.toDebugMessage("Inbound ", chksumReceived,
									"${"0x%02X".format(message[3])} -> ${"0x%02X".format(iddState)}")
									?.also { LOGGER.debug(it) }
								listeners.forEach { it.onMessageReceived(channel, message.toIntArray()) }
							}
							else -> {
								listeners.forEach { it.onMessageReceived(channel, message.toIntArray()) }
							}
						}
					}
				} else {
					Thread.sleep(100L)
				}
			} catch (e: Exception) {
				LOGGER.error("${logTag} ${e.message}", e)
				Thread.sleep(1000)
				disconnectInternal(stayDisconnected = false)
			}
		} catch (e: Exception) {
			LOGGER.warn("${logTag} ${e.message}")
			Thread.sleep(1000)
			disconnectInternal(stayDisconnected = false)
		}
		LOGGER.debug("${logTag} Inbound thread exited")
	}

	private val outboundRunnable: () -> Unit = {
		Thread.currentThread().name = "${channel} Outbound"
		try {
			while (!Thread.interrupted() && state == State.CONNECTED) try {
				if (checksumQueue.isNotEmpty()) {
					idleLoops = 0
					val chksum = checksumQueue.poll()
					var data = listOf(MessageKind.CRC.code.toByte(), chksum).toByteArray()
					data = listOf(data.calculateChecksum().toByte(), MessageKind.CRC.code.toByte(), chksum).toByteArray()
					sendMessage(data)
					data.toDebugMessage("Outbound", lastChecksum, "(checksum queue: ${checksumQueue.size})")
						?.also { LOGGER.debug(it) }
					//listeners.forEach { it.onMessageSent(channel, data, messageQueue.size) }
				} else if (messageQueue.isNotEmpty()) {
					idleLoops = 0
					attempt++
					val (message, delay) = messageQueue.peek()
					val kind = MessageKind.values().find { it.code.toByte() == message[0] }
					val checksum = message.calculateChecksum()
					val data = listOf(checksum.toByte(), *message.toTypedArray()).toByteArray()
					lastChecksum = null
					sendMessage(data)
					data.toDebugMessage("Outbound", lastChecksum, "(attempt: ${attempt})")?.also { LOGGER.debug(it) }

					var timeout = delay ?: MESSAGE_CONFIRMATION_TIMEOUT
					while (lastChecksum != checksum && timeout > 0) {
						Thread.sleep(1)
						timeout--
					}

					val correctlyReceived = checksum == lastChecksum
					if (correctlyReceived) {
						messageQueue.poll()
						attempt = 0
					}
					if (attempt >= MAX_SEND_ATTEMPTS) {
						attempt = 0
						throw ConnectionNotFoundException("Maximum attempts reached")
					}
					when (kind) {
						MessageKind.CRC -> {
							// nothing to do
						}
						MessageKind.IDD -> {
							if (correctlyReceived) {
								data.toDebugMessage("Outbound", lastChecksum, "(remaining: ${messageQueue.size})")
									?.also { LOGGER.debug(it) }
								iddCounter = -5
							} else {
								data.toDebugMessage("Outbound", lastChecksum, "${iddCounter + 1}. ping NOT returned")
									?.also { LOGGER.debug(it) }
								iddCounter++
							}
							if (iddCounter > 4) {
								Thread.sleep(1000)
								disconnectInternal(stayDisconnected = false)
							}
						}
						else -> {
							if (correctlyReceived) {
								data.toDebugMessage("Outbound", lastChecksum,
									"SUCCESS (remaining: ${messageQueue.size})")?.also { LOGGER.debug(it) }
								listeners.forEach { it.onMessageSent(channel, data.toIntArray(), messageQueue.size) }
							}
						}
					}
					if (correctlyReceived) lastChecksum = null
				} else if (iddCounter < 0) {
					Thread.sleep(100L)
					iddCounter++
				} else if (iddCounter == 0) {
					if (onDemand) {
						onDemand = false
						disconnect()
					}
					val state = DeviceIdState.values().find { it.code == iddState }
					if (state != null) sendDeviceStateRequest(state)
					else if (IDD_PING) sendPing()
				} else {
					if (onDemand) {
						onDemand = false
						disconnect()
					}
					Thread.sleep(100L)
				}
			} catch (e: Exception) {
				LOGGER.error("${logTag} ${e.message}")
				Thread.sleep(1000)
				disconnectInternal(stayDisconnected = false)
			}
		} catch (e: Exception) {
			LOGGER.warn("${logTag} ${e.message}")
			Thread.sleep(1000)
			disconnectInternal(stayDisconnected = false)
		}
		LOGGER.debug("${logTag} Outbound thread exited")
	}

	final override fun sendBytes(kind: MessageKind, vararg message: Byte) {
		listeners.forEach { it.onMessagePrepare(channel) }
		if (isDisconnected) {
			onDemand = true
			connectionDescriptor?.let { connect(it) }
		}
		message.let { data ->
			ByteArray(data.size + 1) { i ->
				when (i) {
					0 -> kind.code.toByte()
					else -> data[i - 1]
				}
			}.also { messageQueue.offer(it to kind.delay) }
		}
	}

	/**
	 *  Send ping
	 *
	 *  @see MessageKind.IDD
	 */
	fun sendPing() = sendBytes(MessageKind.IDD, Random.nextBits(8).toByte())

	/**
	 * Request device state.
	 *
	 * @param deviceIdState State to request (1 byte)
	 * @see MessageKind.IDD
	 */
	fun sendDeviceStateRequest(deviceIdState: DeviceIdState) =
		send(MessageKind.IDD, listOf(Random.nextBits(8), deviceIdState.code).toByteArray())

	/**
	 * Connects to a device.
	 *
	 * @param descriptor Descriptor of the device.
	 */
	final override fun connect(descriptor: ConnectionDescriptor?): Boolean {
		connectionRequested = true
		if (descriptor?.let { canConnect(it) } == true || connectionDescriptor?.let { canConnect(it) } == true) {
			LOGGER.debug("${logTag} Connecting ...")
			if (state == State.CONNECTED) disconnectInternal(stayDisconnected = false)

			messageQueue.clear()
			checksumQueue.clear()
			if (descriptor?.let { canConnect(it) } == true) connectionDescriptor = descriptor

			state = State.CONNECTING
			listeners.forEach { it.onConnecting(channel) }

			if (connectorThread?.isAlive != true) {
				connectorThread = Thread(connectorRunnable)
				connectorThread?.name = "${channel}-connector"
				connectorExecutor.execute(connectorThread!!)
			}
			return true
		}
		return false
	}

	private fun reconnect() = disconnectInternal(false)

	/** Disconnects from a device. */
	final override fun disconnect() = disconnectInternal(true)

	private fun disconnectInternal(stayDisconnected: Boolean) {
		LOGGER.debug("${logTag} Disconnecting ...")
		if (stayDisconnected) connectionRequested = false
		state = State.DISCONNECTING
		messageQueue.clear()
		checksumQueue.clear()

		try {
			inboundThread?.takeIf { it.isAlive }?.interrupt()
			outboundThread?.takeIf { it.isAlive }?.interrupt()
			cleanUpConnection()
		} catch (e: IOException) {
			LOGGER.error("${logTag} ${e.message}", e)
		} finally {
			state = State.DISCONNECTED
			listeners.forEach { it.onDisconnect(channel) }
			if (connectionRequested) connectionDescriptor?.also { connect(it) }
		}
	}

	/** Shuts the communicator down completely. */
	final override fun shutdown() {
		disconnectInternal(stayDisconnected = true)
		LOGGER.debug("${logTag} Shutting down communicator ...")
		connectorThread?.takeIf { it.isAlive }?.interrupt()
		connectorExecutor.shutdown()
		connectorExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)
		inboundThread?.takeIf { it.isAlive }?.interrupt()
		inboundExecutor.shutdown()
		inboundExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)
		outboundThread?.takeIf { it.isAlive }?.interrupt()
		outboundExecutor.shutdown()
		inboundExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)
	}

	@Suppress("EXPERIMENTAL_API_USAGE")
	private fun Byte.toUInt() = toUByte().toInt()

	private fun ByteArray.toIntArray() = map { it.toUInt() }.toIntArray()

	private fun ByteArray.toDebugMessage(direction: String, checksum: Int?, message: String = ""): String? {
		val chksumCalculated = this.toList().subList(1, this.size).toByteArray().calculateChecksum()
		if ((CRC_PRINT || this.getOrNull(1)?.toInt() != MessageKind.CRC.code)
			&& (IDD_PING_PRINT || this.getOrNull(1)?.toInt() != MessageKind.IDD.code || this.size > 3)) {
			return "${logTag} ${direction}" +
					"[${checksum?.let { "0x%02X".format(it) } ?: "----"}/${"0x%02X".format(chksumCalculated)}]" +
					" ${MessageKind.values().find { it.code == this.getOrNull(1)?.toInt() } ?: MessageKind.UNKNOWN}" +
					(this.getOrNull(1)?.let { "(0x%02X)".format(it) } ?: "") +
					" ${this.copyOfRange(2, this.size).toDebugString()}" +
					" ${message}"
		}
		return null
	}

	private fun ByteArray.toDebugString(text: Boolean = true) = joinToString(" ") { "0x%02X".format(it) } +
			(takeIf { text }
				?.map { if (it in 0x20..0x7E) it else 0x2E }
				?.toByteArray()?.toString(Charsets.UTF_8)
				?.let { " \"${it}\"" }
				?: "")
}