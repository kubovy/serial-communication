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
import com.poterion.communication.serial.listeners.RgbIndicatorCommunicatorListener
import com.poterion.communication.serial.payload.RgbIndicatorConfiguration
import com.poterion.communication.serial.payload.RgbPattern
import java.awt.Color

/**
 * RGB indicators communicator extension extends the default [Communicator] with [MessageKind.INDICATORS] capabilities.
 * To catch also incoming messages [register] a [RgbIndicatorCommunicatorListener].
 *
 * @see MessageKind.INDICATORS
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class RgbIndicatorCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.INDICATORS) when (message.size) {
			3 -> listeners
				.filterIsInstance<RgbIndicatorCommunicatorListener>()
				.forEach { it.onRgbIndicatorCountChanged(channel, message[2]) }
			13 -> listeners
				.filterIsInstance<RgbIndicatorCommunicatorListener>()
				.forEach {
					val config = RgbIndicatorConfiguration(
						RgbPattern.values().find { p -> p.code == message[5] } ?: RgbPattern.OFF,
						Color(message[6], message[7], message[8]),
						((message[9] and 0xFF) shl 8) or (message[10] and 0xFF),
						message[11], message[12])
					it.onRgbIndicatorConfiguration(channel, message[2], message[3], message[4], config)
				}
		}
	}

	/**
	 * Request RGB indicator strip count.
	 *
	 * @see MessageKind.INDICATORS
	 */
	fun sendRgbIndicatorCountRequest() = send(MessageKind.INDICATORS)

	/**
	 * Send RGB indicator LED request.
	 *
	 * @param num Strip number (1 byte).
	 * @param led LED index (1 byte).
	 * @see MessageKind.INDICATORS
	 */
	fun sendRgbIndicatorLedRequest(num: Int, led: Int) = sendBytes(MessageKind.INDICATORS, num, led)

	/**
	 * Send RGB indicator set all request.
	 *
	 * @param num Strip number (1 byte).
	 * @param color [Color].
	 * @see MessageKind.INDICATORS
	 */
	fun sendRgbIndicatorSetAll(num: Int, color: Color) =
		sendBytes(MessageKind.INDICATORS, num, color.red, color.green, color.blue)

	/**
	 * Send RGB indicator set request.
	 *
	 * @param num Strip number (1 byte).
	 * @param led LED index (1 byte).
	 * @param pattern [RgbPattern].
	 * @param color [Color].
	 * @param delay Delay in ms (2 bytes).
	 * @param min Minimum color value (depends on pattern implementation) (1 byte).
	 * @param max Maximum color value (depends on pattern implementation) (1 byte).
	 * @param replace If true the list of configurations will be replaced by this configuration, otherwise this
	 *                configuration will be appended on the end of the list.
	 * @see MessageKind.INDICATORS
	 */
	fun sendRgbIndicatorSet(num: Int, led: Int, pattern: RgbPattern, color: Color, delay: Int, min: Int, max: Int,
							replace: Boolean = false) =
		sendBytes(MessageKind.INDICATORS, num, led, pattern.code or (if (replace) 0x80 else 0x00),
			color.red, color.green, color.blue, (delay shr 8) and 0xFF, delay and 0xFF, min, max)

	/**
	 * Send RGB indicator set request.
	 *
	 * @param num Strip number (1 byte).
	 * @param led LED index (1 byte).
	 * @param configuration [RgbIndicatorConfiguration].
	 * @param replace If true the list of configurations will be replaced by this configuration, otherwise this
	 *                configuration will be appended on the end of the list.
	 * @see MessageKind.INDICATORS
	 */
	fun sendRgbIndicatorSet(num: Int, led: Int, configuration: RgbIndicatorConfiguration, replace: Boolean = false) =
		sendRgbIndicatorSet(num, led, configuration.pattern, configuration.color, configuration.delay,
			configuration.minimum, configuration.maximum, replace)
}