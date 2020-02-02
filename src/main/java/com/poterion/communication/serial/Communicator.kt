package com.poterion.communication.serial

import com.poterion.communication.serial.Communicator.Companion.MAX_SEND_ATTEMPTS
import com.poterion.communication.serial.Communicator.Companion.MESSAGE_CONFIRMATION_TIMEOUT
import com.poterion.communication.serial.Communicator.State
import javafx.application.Platform
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
 * Any other [MessageKind] will be simply forwarded using the [CommunicatorListener.onMessageReceived] and has to be
 * implemented further.
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
 * @see CommunicatorListener
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
abstract class Communicator<ConnectionDescriptor>(private val channel: Channel) {

	companion object {
		private val LOGGER: Logger = LoggerFactory.getLogger(Communicator::class.java)
		const val IDD_PING = false
		const val MESSAGE_CONFIRMATION_TIMEOUT = 500L // default delay in ms
		const val MAX_SEND_ATTEMPTS = 20
	}

	/**Connection state. */
	enum class State {
		/** Device is disconnected. */
		DISCONNECTED,
		/** Devices is currently connecting but not yet connected. No messages can be still send or received from the
		 *  device. */
		CONNECTING,
		/** Device is connected. Message exchange can be performed. */
		CONNECTED,
		/** No message exchange can be guaranteed. The device is currently being disconnected. */
		DISCONNECTING;
	}

	/** Whether the device is in [State.CONNECTED] or not */
	val isConnected: Boolean
		get() = state == State.CONNECTED

	/** Whether the device is in [State.CONNECTING] or not */
	val isConnecting: Boolean
		get() = state == State.CONNECTING

	/** Whether the device is in [State.DISCONNECTING] or not */
	val isDisconnecting: Boolean
		get() = state == State.DISCONNECTING

	/** Whether the device is in [State.DISCONNECTED] or not */
	val isDisconnected: Boolean
		get() = state == State.DISCONNECTED

	private var connectionRequested = false

	/** Current state of the connection to the device. */
	var state = State.DISCONNECTED
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

	private val listeners = mutableListOf<CommunicatorListener>()

	protected var connectionDescriptor: ConnectionDescriptor? = null
	private var idleLoops = 0

	private val connectorRunnable: () -> Unit = {
		Thread.currentThread().name = "${channel} Connector"
		while (!Thread.interrupted()) {
			if (state == State.CONNECTING) {
				LOGGER.debug("${channel} ${connectionDescriptor}> Connection attempt ...")

				inboundThread?.takeIf { it.isAlive }?.interrupt()
				outboundThread?.takeIf { it.isAlive }?.interrupt()
				cleanUpConnection()

				while (state == State.CONNECTING) try {
					if (createConnection()) {
						iddCounter = 0
						iddState = 0x00

						inboundThread?.takeIf { !it.isInterrupted }?.interrupt()
						outboundThread?.takeIf { !it.isInterrupted }?.interrupt()

						inboundThread = Thread(inboundRunnable)
						inboundThread?.name = "${channel}-inbound"
						outboundThread = Thread(outboundRunnable)
						outboundThread?.name = "${channel}-outbound"

						inboundExecutor.execute(inboundThread!!)
						outboundExecutor.execute(outboundThread!!)

						state = State.CONNECTED
						listeners.forEach { Platform.runLater { it.onConnect(channel) } }
					}
					Thread.sleep(1000)
				} catch (e: Exception) {
					LOGGER.error("${channel} ${connectionDescriptor}> ${e.message}", e)
					Thread.sleep(1000)
					disconnectInternal(stayDisconnected = false)
				}
			} else {
				Thread.sleep(100)
				idleLoops++
				if (idleLoops == 30) {
					if (messageQueue.isEmpty() && checksumQueue.isEmpty()) send(MessageKind.IDD)
					idleLoops = 0
				}
			}
		}
		LOGGER.debug("${channel} ${connectionDescriptor}> Connection thread exited")
	}

	private val inboundRunnable: () -> Unit = {
		Thread.currentThread().name = "${channel} Inbound"
		try {
			while (!Thread.interrupted() && state == State.CONNECTED) try {
				val message = nextMessage()
				idleLoops = 0

				if (message != null) {
					val chksumReceived = message[0].toInt() and 0xFF
					val chksumCalculated = message.toList().subList(1, message.size).toByteArray().calculateChecksum()
					LOGGER.debug("${channel} ${connectionDescriptor}> Inbound  RAW" +
							" [${"0x%02X".format(chksumReceived)}/${"0x%02X".format(chksumCalculated)}]:" +
							" ${message.joinToString(" ") { "0x%02X".format(it) }}"
					)

					if (chksumCalculated == chksumReceived) {
						val messageKind = message[1]
							.let { byte -> MessageKind.values().find { it.code.toByte() == byte } }
							?: MessageKind.UNKNOWN

						if (messageKind != MessageKind.CRC) checksumQueue.add(chksumCalculated.toByte())

						when (messageKind) {
							MessageKind.CRC -> {
								lastChecksum = (message[2].toInt() and 0xFF)
								LOGGER.debug("${channel} ${connectionDescriptor}>" +
										" Inbound  [CRC] ${"0x%02X".format(lastChecksum)}")
							}
							MessageKind.IDD -> {
								if (message.size > 3) iddState = message[3].toUInt() + 1
								LOGGER.debug("${channel} ${connectionDescriptor}>" +
										" Inbound  [IDD] ${"0x%02X".format(iddState)}")
								listeners.forEach {
									Platform.runLater { it.onMessageReceived(channel, message.toIntArray()) }
								}
							}
							else -> {
								LOGGER.debug("${channel} ${connectionDescriptor}> Inbound "
										+ " [${messageKind.name}]"
										+ " ${message.joinToString(" ") { "0x%02X".format(it) }}")
								listeners.forEach {
									Platform.runLater { it.onMessageReceived(channel, message.toIntArray()) }
								}
							}
						}
					}
				} else {
					Thread.sleep(100L)
				}
			} catch (e: Exception) {
				LOGGER.error("${channel} ${connectionDescriptor}> ${e.message}")
				Thread.sleep(1000)
				disconnectInternal(stayDisconnected = false)
			}
		} catch (e: Exception) {
			LOGGER.warn("${channel} ${connectionDescriptor}> ${e.message}")
			Thread.sleep(1000)
			disconnectInternal(stayDisconnected = false)
		}
		LOGGER.debug("${channel} ${connectionDescriptor}> Inbound thread exited")
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
					LOGGER.debug("${channel} ${connectionDescriptor}> Outbound [CRC] ${"0x%02X".format(chksum)}"
							+ " (checksum queue: ${checksumQueue.size})")
					//listeners.forEach { Platform.runLater { it.onMessageSent(channel, data, messageQueue.size) } }
				} else if (messageQueue.isNotEmpty()) {
					idleLoops = 0
					attempt++
					val (message, delay) = messageQueue.peek()
					val kind = MessageKind.values().find { it.code.toByte() == message[0] }
					val checksum = message.calculateChecksum()
					val data = listOf(checksum.toByte(), *message.toTypedArray()).toByteArray()
					lastChecksum = null
					sendMessage(data)

					LOGGER.debug("${channel} ${connectionDescriptor}> Outbound"
							+ " [${"0x%02X".format(lastChecksum)}/${"0x%02X".format(checksum)}]"
							+ " ${data.joinToString(" ") { "0x%02X".format(it) }} (attempt: ${attempt})"
					)


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
								LOGGER.debug("${channel} ${connectionDescriptor}> Outbound" +
										" [${"0x%02X".format(lastChecksum)}/${"0x%02X".format(checksum)}]:" +
										" ${data.joinToString(" ") { "0x%02X".format(it) }}" +
										" (remaining: ${messageQueue.size})")
								iddCounter = -5
							} else {
								LOGGER.debug("${channel} ${connectionDescriptor}> ${iddCounter + 1}. ping not returned")
								iddCounter++
							}
							if (iddCounter > 4) {
								Thread.sleep(1000)
								disconnectInternal(stayDisconnected = false)
							}
						}
						else -> {
							if (correctlyReceived) {
								LOGGER.debug("${channel} ${connectionDescriptor}> Outbound"
										+ " [${"0x%02X".format(lastChecksum)}/${"0x%02X".format(checksum)}]:"
										+ " ${data.joinToString(" ") { "0x%02X".format(it) }}"
										+ " SUCCESS (queue: ${messageQueue.size})")
								listeners
										.forEach { Platform.runLater { it.onMessageSent(channel, data.toIntArray(), messageQueue.size) } }
							}
						}
					}
					if (correctlyReceived) lastChecksum = null
				} else if (iddCounter < 0) {
					Thread.sleep(100L)
					iddCounter++
				} else if (iddCounter == 0) {

					val message = if (iddState < 0x02) arrayOf(MessageKind.IDD.code, Random.nextBits(4), iddState)
					else if (IDD_PING) arrayOf(MessageKind.IDD.code, Random.nextBits(4))
					else null

					if (message != null) messageQueue.add(message.map { it.toByte() }.toByteArray() to 500)
				} else {
					Thread.sleep(100L)
				}
			} catch (e: Exception) {
				LOGGER.error("${channel} ${connectionDescriptor}> ${e.message}")
				Thread.sleep(1000)
				disconnectInternal(stayDisconnected = false)
			}
		} catch (e: Exception) {
			LOGGER.warn("${channel} ${connectionDescriptor}> ${e.message}")
			Thread.sleep(1000)
			disconnectInternal(stayDisconnected = false)
		}
		LOGGER.debug("${channel} ${connectionDescriptor}> Outbound thread exited")
	}

	/**
	 * Whether the communicator can connect or not.
	 *
	 * @return True, all conditions to establish a connection are met.
	 */
	open fun canConnect(descriptor: ConnectionDescriptor): Boolean = state == State.DISCONNECTED
			|| connectionDescriptor != descriptor

	/**
	 * Creates a new connection.
	 *
	 * @return True, if a new connection was established.
	 */
	abstract fun createConnection(): Boolean

	/** Cleans up connection after disconnecting. */
	abstract fun cleanUpConnection()

	/**
	 * Next message getter.
	 *
	 * @return New available message on the channel or <code>null</code> if no new message is available.
	 */
	abstract fun nextMessage(): ByteArray?

	/**
	 * Sends a message through the channel.
	 *
	 * @param data The message.
	 */
	abstract fun sendMessage(data: ByteArray)

	/**
	 * Queues a new message to be sent to target device.
	 *
	 * @param kind Message kind.
	 * @param message Message.
	 */
	fun send(kind: MessageKind, message: ByteArray = byteArrayOf()) = message
			.let { data ->
				ByteArray(data.size + 1) { i ->
					when (i) {
						0 -> kind.code.toByte()
						else -> data[i - 1]
					}
				}.also { messageQueue.offer(it to kind.delay) }
			}

	/**
	 * Register a new listener.
	 *
	 * @param listener Listener to register.
	 */
	fun register(listener: CommunicatorListener) {
		if (!listeners.contains(listener)) listeners.add(listener)
	}

	/**
	 * Unregister an existing listener.
	 *
	 * @param listener Listener to unregister.
	 */
	fun unregister(listener: CommunicatorListener) = listeners.remove(listener)

	/**
	 * Connects to a device.
	 *
	 * @param descriptor Descriptor of the device.
	 */
	fun connect(descriptor: ConnectionDescriptor): Boolean {
		connectionRequested = true
		if (canConnect(descriptor)) {
			LOGGER.debug("${channel} ${connectionDescriptor}> Connecting ...")
			if (state == State.CONNECTED) disconnectInternal(stayDisconnected = false)

			messageQueue.clear()
			checksumQueue.clear()
			connectionDescriptor = descriptor

			state = State.CONNECTING
			listeners.forEach { Platform.runLater { it.onConnecting(channel) } }

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
	fun disconnect() = disconnectInternal(true)

	private fun disconnectInternal(stayDisconnected: Boolean) {
		LOGGER.debug("${channel} ${connectionDescriptor}> Disconnecting ...")
		if (stayDisconnected) connectionRequested = false
		state = State.DISCONNECTING
		messageQueue.clear()
		checksumQueue.clear()

		try {
			inboundThread?.takeIf { it.isAlive }?.interrupt()
			outboundThread?.takeIf { it.isAlive }?.interrupt()
			cleanUpConnection()
		} catch (e: IOException) {
			LOGGER.error("${channel} ${connectionDescriptor}> ${e.message}", e)
		} finally {
			state = State.DISCONNECTED
			listeners.forEach { Platform.runLater { it.onDisconnect(channel) } }
			if (connectionRequested) connectionDescriptor?.also { connect(it) }
		}
	}

	/** Shuts the communicator down completely. */
	open fun shutdown() {
		disconnectInternal(stayDisconnected = true)
		LOGGER.debug("${channel} ${connectionDescriptor}> Shutting down communicator ...")
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
}