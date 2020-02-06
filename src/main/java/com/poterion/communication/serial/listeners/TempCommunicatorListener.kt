package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel

/**
 * Temperature & humidity communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.TEMP
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface TempCommunicatorListener: CommunicatorListener {
	/**
	 * Temperature/humidity sensor count received callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param count Sensor count.
	 * @see com.poterion.communication.serial.MessageKind.TEMP
	 */
	fun onTempCountReceived(channel: Channel, count: Int)

	/**
	 * Temperature/humidity callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num Sensor number.
	 * @param temp Temperature in &deg;C
	 * @param humidity Relative humidity in %
	 * @see com.poterion.communication.serial.MessageKind.TEMP
	 */
	fun onTempReceived(channel: Channel, num: Int, temp: Double, humidity: Double)
}