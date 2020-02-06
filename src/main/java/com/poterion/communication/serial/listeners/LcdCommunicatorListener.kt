package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.payload.LcdCommand

/**
 * LCD communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.LCD
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface LcdCommunicatorListener: CommunicatorListener {
	/**
	 * LCD count received callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param count LCD count.
	 * @see com.poterion.communication.serial.MessageKind.LCD
	 */
	fun onLcdCountReceived(channel: Channel, count: Int)

	/**
	 * LCD command received callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num LCD number.
	 * @param command [LcdCommand]
	 * @see com.poterion.communication.serial.MessageKind.LCD
	 */
	fun onLcdCommandReceived(channel: Channel, num: Int, command: LcdCommand)

	/**
	 * LCD content callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num LCD number.
	 * @param backlight Backlight on/off.
	 * @param line Line number.
	 * @param content Content.
	 * @see com.poterion.communication.serial.MessageKind.LCD
	 */
	fun onLcdContentChanged(channel: Channel, num: Int, backlight: Boolean, line: Int, content: String)
}