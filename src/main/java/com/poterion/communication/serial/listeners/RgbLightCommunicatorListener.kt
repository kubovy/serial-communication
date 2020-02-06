package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.payload.RgbLightConfiguration

/**
 * RGB light communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.LIGHT
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface RgbLightCommunicatorListener: CommunicatorListener {
	/**
	 * RGB light count changed callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param count Strip count.
	 * @see com.poterion.communication.serial.MessageKind.LIGHT
	 */
	fun onRgbLightCountChanged(channel: Channel, count: Int)

	/**
	 * RGB strip configuration callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num Strip number.
	 * @param count Configuration count.
	 * @param index Configuration index.
	 * @param configuration [RgbLightConfiguration].
	 * @see com.poterion.communication.serial.MessageKind.LIGHT
	 */
	fun onRgbLightConfiguration(channel: Channel, num: Int, count: Int, index: Int,
								configuration: RgbLightConfiguration)
}