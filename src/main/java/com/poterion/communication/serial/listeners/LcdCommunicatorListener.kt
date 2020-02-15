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