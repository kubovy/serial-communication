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

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.listeners.IOCommunicatorListener

/**
 * IO communicator extension extends the default [Communicator] with [MessageKind.IO] capabilities.
 * To catch also incoming messages [register] a [IOCommunicatorListener].
 *
 * @see MessageKind.IO
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class IOCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.IO && message.size == 4) listeners
			.filterIsInstance<IOCommunicatorListener>()
			.forEach { it.onIoChanged(channel, message[2], message[3] > 0x00) }
	}

	/**
	 * Request IO state.
	 *
	 * @param port IO number (1 byte).
	 * @see MessageKind.IO
	 */
	fun sendIoRequest(port: Int) = sendBytes(MessageKind.IO, port)

	/**
	 * Write IO state.
	 *
	 * @param port IO number (1 byte).
	 * @param value Value (1 bit).
	 * @see MessageKind.IO
	 */
	fun sendIoWrite(port: Int, value: Boolean) = sendBytes(MessageKind.IO, port, if (value) 0x81 else 0x80)
}