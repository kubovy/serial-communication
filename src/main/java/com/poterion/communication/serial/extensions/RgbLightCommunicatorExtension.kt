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

import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.listeners.RgbLightCommunicatorListener
import com.poterion.communication.serial.payload.ColorOrder
import com.poterion.communication.serial.payload.RgbLightConfiguration
import com.poterion.communication.serial.payload.RgbLightPattern
import com.poterion.communication.serial.toColor
import com.poterion.communication.serial.toComponents
import java.awt.Color

/**
 * RGB light communicator extension extends the default [Communicator] with [MessageKind.LIGHT] capabilities.
 * To catch also incoming messages [register] a [RgbLightCommunicatorListener].
 *
 * @param communicator Communicator to extend
 * @param colorOrder Color order getter
 * @see MessageKind.LIGHT
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class RgbLightCommunicatorExtension<ConnectionDescriptor>(
		communicator: Communicator<ConnectionDescriptor>,
		private val colorOrder: (Int) -> ColorOrder = { ColorOrder.RGB }) :

		CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.LIGHT) when (message.size) {
			3 -> listeners
					.filterIsInstance<RgbLightCommunicatorListener>()
					.forEach { it.onRgbLightCountChanged(channel, message[2]) }
			34 -> listeners
					.filterIsInstance<RgbLightCommunicatorListener>()
					.forEach {
					val config = RgbLightConfiguration(
							RgbLightPattern.values().find { p -> p.code == message[5] } ?: RgbLightPattern.OFF,
							message.toColor(colorOrder(message[2]), 6),
							message.toColor(colorOrder(message[2]), 9),
							message.toColor(colorOrder(message[2]), 12),
							message.toColor(colorOrder(message[2]), 15),
							message.toColor(colorOrder(message[2]), 18),
							message.toColor(colorOrder(message[2]), 21),
							message.toColor(colorOrder(message[2]), 24),
							((message[27] and 0xFF) shl 8) or (message[28] and 0xFF),
							message[29], message[30], message[31], message[32], message[33])
					it.onRgbLightConfiguration(channel, message[2], message[3], message[4], config)
				}
		}
	}

	/**
	 * Request RGB light strip count.
	 *
	 * @see MessageKind.RGB
	 */
	fun sendRgbLightCountRequest() = send(MessageKind.LIGHT)

	/**
	 * Send RGB light request configuration.
	 *
	 * @param num Strip number (1 byte).
	 * @param index Configuration index (1 byte). If not present all configurations will be requested.
	 * @see MessageKind.LIGHT
	 */
	fun sendRgbLightItemRequest(num: Int, index: Int? = null) = sendBytes(MessageKind.LIGHT, num, index ?: 0x80)

	/**
	 * Send RGB light set request.
	 *
	 * @param num Strip number (1 byte).
	 * @param pattern [RgbLightPattern].
	 * @param color1 [Color] 1.
	 * @param color2 [Color] 2.
	 * @param color3 [Color] 3.
	 * @param color4 [Color] 4.
	 * @param color5 [Color] 5.
	 * @param color6 [Color] 6.
	 * @param color7 [Color] 7.
	 * @param delay Delay in ms (2 bytes).
	 * @param width Width (depends on pattern implementation) (1 byte).
	 * @param fading Fading (depends on pattern implementation) (1 byte).
	 * @param min Minimum color value (depends on pattern implementation) (1 byte).
	 * @param max Maximum color value (depends on pattern implementation) (1 byte).
	 * @param replace If true the list of configurations will be replaced by this configuration, otherwise this
	 *                configuration will be appended on the end of the list.
	 * @see MessageKind.LIGHT
	 */
	fun sendRgbLightSet(num: Int, pattern: RgbLightPattern, color1: Color, color2: Color, color3: Color, color4: Color,
						color5: Color, color6: Color, color7: Color, delay: Int, width: Int, fading: Int, min: Int,
						max: Int, timeout: Int, replace: Boolean = false) =
			sendBytes(MessageKind.LIGHT, num, pattern.code or (if (replace) 0x80 else 0x00),
					*color1.toComponents(colorOrder(num)),
					*color2.toComponents(colorOrder(num)),
					*color3.toComponents(colorOrder(num)),
					*color4.toComponents(colorOrder(num)),
					*color5.toComponents(colorOrder(num)),
					*color6.toComponents(colorOrder(num)),
					*color7.toComponents(colorOrder(num)),
					(delay shr 8) and 0xFF, delay and 0xFF,
					width, fading, min, max, timeout)

	/**
	 * Send RGB light set request.
	 *
	 * @param num Strip number (1 byte).
	 * @param configuration [RgbLightConfiguration].
	 * @param replace If true the list of configurations will be replaced by this configuration, otherwise this
	 *                configuration will be appended on the end of the list.
	 * @see MessageKind.LIGHT
	 */
	fun sendRgbLightSet(num: Int, configuration: RgbLightConfiguration, replace: Boolean = false) =
		sendRgbLightSet(num, configuration.pattern, configuration.color1, configuration.color2, configuration.color3,
				configuration.color4, configuration.color5, configuration.color6, configuration.color7,
				configuration.delay, configuration.width, configuration.fading, configuration.minimum,
				configuration.maximum, configuration.timeout, replace)
}