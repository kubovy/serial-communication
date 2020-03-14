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

import com.poterion.communication.serial.*
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.listeners.DataCommunicatorListener

/**
 * Data communicator extension extends the default [Communicator] with [MessageKind.DATA] and
 * [MessageKind.CONSISTENCY_CHECK] capabilities.
 * To catch also incoming messages [register] a [DataCommunicatorListener].
 *
 * @see MessageKind.CONSISTENCY_CHECK
 * @see MessageKind.DATA
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class DataCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		when (messageKind) {
			MessageKind.CONSISTENCY_CHECK -> if (message.size == 4) listeners
				.filterIsInstance<DataCommunicatorListener>()
				.forEach { it.onConsistencyCheckReceived(channel, message[2], message[3]) }
			MessageKind.DATA -> if (message.size > 7) listeners
				.filterIsInstance<DataCommunicatorListener>()
				.forEach {
					it.onDataReceived(channel, message[2], (message[3] to message[4]).toDoubleInt(),
						(message[5] to message[6]).toDoubleInt(), message.copyOfRange(7, message.size))
				}
			else -> {
				// noop
			}
		}
	}

	/**
	 * Request consistency check.
	 *
	 * @param part Data part (1 byte).
	 * @see MessageKind.CONSISTENCY_CHECK
	 */
	fun sendConsistencyCheckRequest(part: Int) = sendBytes(MessageKind.CONSISTENCY_CHECK, part)

	/**
	 * Request data.
	 *
	 * @param part Part of data to request (1 byte).
	 * @see MessageKind.DATA
	 */
	fun sendDataRequest(part: Int) = sendBytes(MessageKind.DATA, part)

	/**
	 * Request data.
	 *
	 * @param part Part of data to request (1 byte).
	 * @param address Starting address (2 bytes).
	 * @param length Requested length (2 bytes).
	 * @see MessageKind.DATA
	 */
	fun sendDataRequest(part: Int, address: Int, length: Int) = sendBytes(MessageKind.DATA, part,
		(address shr 8) and 0xFF, address and 0xFF, (length shr 8) and 0xFF, length and 0xFF)

	/**
	 * Request data.
	 *
	 * @param part Part of data to request (1 byte).
	 * @see MessageKind.DATA
	 */
	fun sendData(part: Int, data: ByteArray) = sendBytes(MessageKind.DATA, part.toByte(), *data)
}