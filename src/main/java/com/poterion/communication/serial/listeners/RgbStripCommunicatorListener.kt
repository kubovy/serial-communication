package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.payload.RgbStripConfiguration

/**
 * RGB strip communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.RGB
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface RgbStripCommunicatorListener: CommunicatorListener {

	/**
	 * RGB strip count changed callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param count Strip count.
	 * @see com.poterion.communication.serial.MessageKind.RGB
	 */
	fun onRgbStripCountChanged(channel: Channel, count: Int)

	/**
	 * RGB strip configuration callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num Strip number.
	 * @param count Configuration count.
	 * @param index Configuration index.
	 * @param configuration Configuration.
	 * @see com.poterion.communication.serial.MessageKind.RGB
	 */
	fun onRgbStripConfiguration(channel: Channel, num: Int, count: Int, index: Int,
								configuration: RgbStripConfiguration)
}