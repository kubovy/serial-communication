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
import com.poterion.communication.serial.listeners.RegistryCommunicatorListener

/**
 * Registry communicator extension extends the default [Communicator] with [MessageKind.REGISTRY] capabilities.
 * To catch also incoming messages [register] a [RegistryCommunicatorListener].
 *
 * @see MessageKind.REGISTRY
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class RegistryCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.REGISTRY) when {
			message.size == 5 -> listeners
				.filterIsInstance<RegistryCommunicatorListener>()
				.forEach { it.onRegistryValue(channel, message[2], message[3], message[4]) }
			message.size > 5 -> listeners
				.filterIsInstance<RegistryCommunicatorListener>()
				.forEach { it.onRegistryValue(channel, message[2], message[3], *message.copyOfRange(5, message.size)) }
		}
	}

	/**
	 * Send registry request.
	 *
	 * @param address Address (1 byte).
	 * @param registry Registry (1 byte). Not present will request all registries.
	 * @see MessageKind.REGISTRY
	 */
	fun sendRegistryRequest(address: Int, registry: Int? = null) =
		send(MessageKind.REGISTRY, listOfNotNull(address, registry).toByteArray())

	/**
	 * Write registry.
	 *
	 * @param address Address (1 byte).
	 * @param registry Registry (1 byte).
	 * @param values One or more values. Multiple values will write in consecutive registries.
	 * @see MessageKind.REGISTRY
	 */
	fun sendRegistryWrite(address: Int, registry: Int, vararg values: Int) = send(MessageKind.REGISTRY,
		listOfNotNull(address, registry, values.size.takeIf { it > 1 }, *values.toTypedArray()).toByteArray())
}