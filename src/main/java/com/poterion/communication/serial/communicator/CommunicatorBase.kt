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

import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.listeners.CommunicatorListener
import com.poterion.communication.serial.payload.DeviceCapabilities

/**
 * Communicator interface.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class CommunicatorBase<ConnectionDescriptor> {

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

	/** Current state of the connection to the device. */
	abstract val state: State

	/** Detected device's capabilities. */
	abstract val deviceCapabilities: DeviceCapabilities

	/** Detected device's name */
	abstract val deviceName: String

	/** Registered listeners */
	internal open val listeners = mutableListOf<CommunicatorListener>()

	/** Connection descriptor. */
	open var connectionDescriptor: ConnectionDescriptor? = null
		set(value) {
			if (value == null || canConnect(value)) field = value
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
	fun send(kind: MessageKind, message: ByteArray = byteArrayOf()) = sendBytes(kind, *message)

	abstract fun sendBytes(kind: MessageKind, vararg message: Byte)

	fun sendBytes(kind: MessageKind, vararg message: Int) = sendBytes(kind, *message.map { it.toByte() }.toByteArray())

	/**
	 * Register a new listener.
	 *
	 * @param listener Listener to register.
	 */
	open fun register(listener: CommunicatorListener) {
		if (!listeners.contains(listener)) listeners.add(listener)
	}

	/**
	 * Unregister an existing listener.
	 *
	 * @param listener Listener to unregister.
	 */
	open fun unregister(listener: CommunicatorListener) = listeners.remove(listener)

	/**
	 * Connects to a device.
	 *
	 * @param descriptor Descriptor of the device.
	 */
	abstract fun connect(descriptor: ConnectionDescriptor? = null): Boolean

	/** Disconnects from a device. */
	abstract fun disconnect()

	/** Shuts the communicator down completely. */
	abstract fun shutdown()
}