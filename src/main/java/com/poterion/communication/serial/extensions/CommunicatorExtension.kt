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
@file:Suppress("unused")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.communicator.CommunicatorBase
import com.poterion.communication.serial.communicator.State
import com.poterion.communication.serial.listeners.CommunicatorListener
import com.poterion.communication.serial.payload.DeviceCapabilities

/**
 * Communicator extension allows to easily implement protocol extension to the [Communicator] using the builder pattern.
 *
 * @param ConnectionDescriptor Connection descriptor based on the extended [Communicator]
 * @param communicator The extended communicator
 */
abstract class CommunicatorExtension<ConnectionDescriptor>(

	protected val communicator: Communicator<ConnectionDescriptor>):

	CommunicatorBase<ConnectionDescriptor>(),
	CommunicatorListener {

	init {
		startup()
	}

	final override val channel: Channel
		get() = communicator.channel

	final override val state: State
		get() = communicator.state

	final override val deviceCapabilities: DeviceCapabilities
		get() = communicator.deviceCapabilities

	final override val deviceName: String
		get() = communicator.deviceName

	final override val listeners: MutableList<CommunicatorListener>
		get() = communicator.listeners

	override var connectionDescriptor: ConnectionDescriptor?
		get() = communicator.connectionDescriptor
		set(value) {
			communicator.connectionDescriptor = value
		}

	private fun startup() {
		communicator.register(this)
	}

	final override fun createConnection(): Boolean = communicator.createConnection()

	final override fun cleanUpConnection() = communicator.cleanUpConnection()

	final override fun nextMessage(): ByteArray? = communicator.nextMessage()

	final override fun sendMessage(data: ByteArray) = communicator.sendMessage(data)

	final override fun sendBytes(kind: MessageKind, vararg message: Byte) = communicator.sendBytes(kind, *message)

	override fun register(listener: CommunicatorListener) = communicator.register(listener)

	override fun unregister(listener: CommunicatorListener): Boolean = communicator.unregister(listener)

	final override fun connect(descriptor: ConnectionDescriptor?): Boolean = communicator.connect(descriptor)

	final override fun disconnect() = communicator.disconnect()

	final override fun shutdown() = communicator.shutdown()

	override fun onConnecting(channel: Channel) {
		// noop
	}

	override fun onConnect(channel: Channel) {
		// noop
	}

	override fun onConnectionReady(channel: Channel) {
		// noop
	}

	override fun onDisconnect(channel: Channel) {
		// noop
	}

	final override fun onMessageReceived(channel: Channel, message: IntArray) {
		val messageKind = MessageKind.values().find { it.code == message[1] } ?: MessageKind.UNKNOWN
		onMessageKindReceived(channel, messageKind, message)
	}

	/**
	 * On message callback including detected [MessageKind]
	 *
	 * @param channel Channel triggering the event.
	 * @param messageKind [MessageKind].
	 * @param message Received message (whole, including CRC and KIND bytes).
	 */
	abstract fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray)

	override fun onMessagePrepare(channel: Channel) {
		// noop
	}

	override fun onMessageSent(channel: Channel, message: IntArray, remaining: Int) {
		// noop
	}

	override fun onDeviceCapabilitiesChanged(channel: Channel, capabilities: DeviceCapabilities) {
		// noop
	}

	override fun onDeviceNameChanged(channel: Channel, name: String) {
		// noop
	}
}