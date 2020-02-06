@file:Suppress("unused", "MemberVisibilityCanBePrivate")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.listeners.RgbIndicatorCommunicatorListener
import com.poterion.communication.serial.listeners.RgbLightCommunicatorListener
import com.poterion.communication.serial.payload.RgbLightConfiguration
import com.poterion.communication.serial.payload.RgbLightPattern
import java.awt.Color

/**
 * RGB light communicator extension extends the default [Communicator] with [MessageKind.LIGHT] capabilities.
 * To catch also incoming messages [register] a [RgbLightCommunicatorListener].
 *
 * @see MessageKind.LIGHT
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class RgbLightCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
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
						Color(message[6], message[7], message[8]),
						Color(message[9], message[10], message[11]),
						Color(message[12], message[13], message[14]),
						Color(message[15], message[16], message[17]),
						Color(message[18], message[19], message[20]),
						Color(message[21], message[22], message[23]),
						Color(message[24], message[25], message[26]),
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
						color5: Color, color6: Color, delay: Int, width: Int, fading: Int, min: Int, max: Int,
						timeout: Int, replace: Boolean = false) =
		sendBytes(MessageKind.LIGHT, num, pattern.code or (if (replace) 0x80 else 0x00),
			color1.red, color1.green, color1.blue,
			color2.red, color2.green, color2.blue,
			color3.red, color3.green, color3.blue,
			color4.red, color4.green, color4.blue,
			color5.red, color5.green, color5.blue,
			color6.red, color6.green, color6.blue,
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
			configuration.color4, configuration.color5, configuration.color6, configuration.delay, configuration.width,
			configuration.fading, configuration.minimum, configuration.maximum, configuration.timeout, replace)
}