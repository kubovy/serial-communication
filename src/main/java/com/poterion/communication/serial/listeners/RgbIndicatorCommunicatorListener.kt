package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.payload.RgbIndicatorConfiguration

/**
 * RGB indicators communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.INDICATORS
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface RgbIndicatorCommunicatorListener: CommunicatorListener {
	/**
	 * RGB strip count changed callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param count Strip count.
	 * @see com.poterion.communication.serial.MessageKind.RGB
	 */
	fun onRgbIndicatorCountChanged(channel: Channel, count: Int)

	/**
	 * RGB strip configuration callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num Strip number.
	 * @param count Configuration count.
	 * @param index Configuration index.
	 * @param configuration [RgbIndicatorConfiguration].
	 * @see com.poterion.communication.serial.MessageKind.INDICATORS
	 */
	fun onRgbIndicatorConfiguration(channel: Channel, num: Int, count: Int, index: Int,
									configuration: RgbIndicatorConfiguration)
}