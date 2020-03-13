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
package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.payload.DeviceCapabilities

/**
 * Communication listener interface enables other part of the application to use the
 * [com.poterion.communication.serial.communicator.Communicator] to communicate with a peripheral device.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface CommunicatorListener {
	/**
	 * On connecting callback
	 *
	 * @param channel Channel triggering the event.
	 */
	fun onConnecting(channel: Channel)

	/**
	 * On connection established callback
	 *
	 * @param channel Channel triggering the event.
	 */
	fun onConnect(channel: Channel)

	/**
	 * On ready callback.
	 *
	 * @param channel Channel triggering the event.
	 */
	fun onConnectionReady(channel: Channel)

	/**
	 * On connection lost callback
	 *
	 * @param channel Channel triggering the event.
	 */
	fun onDisconnect(channel: Channel)

	/**
	 * On message callback
	 *
	 * @param channel Channel triggering the event.
	 * @param message Received message
	 */
	fun onMessageReceived(channel: Channel, message: IntArray)

	/**
	 * On message preparation.
	 *
	 * @param channel Channel triggering the event.
	 */
	fun onMessagePrepare(channel: Channel)

	/**
	 * On message sent callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param message Sent raw message
	 * @param remaining Remaining message count in the queue.
	 */
	fun onMessageSent(channel: Channel, message: IntArray, remaining: Int)

	/**
	 * Device capabilities changed callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param capabilities [DeviceCapabilities]
	 * @see com.poterion.communication.serial.MessageKind.IDD
	 */
	fun onDeviceCapabilitiesChanged(channel: Channel, capabilities: DeviceCapabilities)

	/**
	 * Device name changed callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param name Name of the device.
	 * @see com.poterion.communication.serial.MessageKind.IDD
	 */
	fun onDeviceNameChanged(channel: Channel, name: String)
}