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
@file:Suppress("unused", "MemberVisibilityCanBePrivate")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.listeners.RgbStripCommunicatorListener
import com.poterion.communication.serial.payload.RgbPattern
import com.poterion.communication.serial.payload.RgbStripConfiguration
import java.awt.Color

/**
 * RGB strip communicator extension extends the default [Communicator] with [MessageKind.RGB] capabilities.
 * To catch also incoming messages [register] a [RgbStripCommunicatorListener].
 *
 * @see MessageKind.RGB
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class RgbStripCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.RGB) when (message.size) {
			3 -> listeners
				.filterIsInstance<RgbStripCommunicatorListener>()
				.forEach { it.onRgbStripCountChanged(channel, message[2]) }
			14 -> listeners
				.filterIsInstance<RgbStripCommunicatorListener>()
				.forEach {
					val config = RgbStripConfiguration(
						RgbPattern.values().find { p -> p.code == message[5] } ?: RgbPattern.OFF,
						Color(message[6], message[7], message[8]),
						((message[9] and 0xFF) shl 8) or (message[10] and 0xFF),
						message[11], message[12], message[13])
					it.onRgbStripConfiguration(channel, message[2], message[3], message[4], config)
				}
		}
	}

	/**
	 * Request RGB strip count.
	 *
	 * @see MessageKind.RGB
	 */
	fun sendRgbStripCountRequest() = send(MessageKind.RGB)

	/**
	 * Request RGB strip configuration.
	 *
	 * @param num Strip number (1 byte).
	 * @param index Configuration index (7 bits). If not present all configurations are requested.
	 * @see MessageKind.RGB
	 */
	fun sendRgbStripConfigurationRequest(num: Int, index: Int? = null) = sendBytes(MessageKind.RGB, num, index ?: 0x80)

	/**
	 * Send RGB strip configuration.
	 *
	 * @param num Strip number (1 byte).
	 * @param pattern [RgbPattern]
	 * @param color [Color]
	 * @param delay Delay in ms (2 bytes)
	 * @param min Minimum color value (depends on pattern implementation) (1 byte).
	 * @param max Maximum color value (depends on pattern implementation) (1 byte).
	 * @param timeout Number of times to repeat pattern animation before switching to next item in the list (1 byte).
	 * @param replace If true the list of configurations will be replaced by this configuration, otherwise this
	 *                configuration will be appended on the end of the list.
	 * @see MessageKind.RGB
	 */
	fun sendRgbStripConfiguration(num: Int, pattern: RgbPattern, color: Color, delay: Int, min: Int, max: Int,
								  timeout: Int, replace: Boolean = false) =
		sendBytes(MessageKind.RGB, num, pattern.code or (if (replace) 0x80 else 0x00),
			color.red, color.green, color.blue, (delay shr 8) and 0xFF, delay and 0xFF, min, max, timeout)

	/**
	 * Send RGB strip configuration.
	 *
	 * @param num Strip number (1 byte).
	 * @param configuration [RgbStripConfiguration].
	 * @param replace If true the list of configurations will be replaced by this configuration, otherwise this
	 *                configuration will be appended on the end of the list.
	 * @see MessageKind.RGB
	 */
	fun sendRgbStripConfiguration(num: Int, configuration: RgbStripConfiguration, replace: Boolean = false) =
		sendRgbStripConfiguration(num, configuration.pattern, configuration.color, configuration.delay,
			configuration.minimum, configuration.maximum, configuration.timeout, replace)
}