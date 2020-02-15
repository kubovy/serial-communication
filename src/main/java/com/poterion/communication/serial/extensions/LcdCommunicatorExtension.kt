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
@file:Suppress("unused")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.listeners.LcdCommunicatorListener
import com.poterion.communication.serial.payload.LcdCommand
import com.poterion.communication.serial.toByteArray
import com.poterion.communication.serial.toString

/**
 * LCD communicator extension extends the default [Communicator] with [MessageKind.LCD] capabilities.
 * To catch also incoming messages [register] a [LcdCommunicatorListener].
 *
 * @see MessageKind.LCD
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class LcdCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.LCD) when {
			message.size == 3 -> listeners
				.filterIsInstance<LcdCommunicatorListener>()
				.forEach { it.onLcdCountReceived(channel, message[2]) }
			message.size == 5 && message[3] == 0xFF -> LcdCommand.values().find { it.code == message[4] }?.also { cmd ->
				listeners
					.filterIsInstance<LcdCommunicatorListener>()
					.forEach { it.onLcdCommandReceived(channel, message[2], cmd) }
			}
			message.size > 6 -> listeners
				.filterIsInstance<LcdCommunicatorListener>()
				.forEach {
					it.onLcdContentChanged(channel, message[2],
						message[3] == LcdCommand.BACKLIGHT.code, message[4],
						message.copyOfRange(5, message.size).toString(Charsets.UTF_8))
				}
		}
	}

	/**
	 * Request LCD count.
	 *
	 * @see MessageKind.LCD
	 */
	fun sendLcdCountRequest() = send(MessageKind.LCD)

	/**
	 * Request LCD content.
	 *
	 * @param num LCD number (1 byte).
	 * @param line Line number (7 bits). If not set all lines will be requested.
	 * @see MessageKind.LCD
	 */
	fun sendLcdContentRequest(num: Int, line: Int? = null) =
		send(MessageKind.LCD, listOfNotNull(num, line ?: 0x80).toByteArray())

	/**
	 * Send LCD command.
	 *
	 * @param num LCD number (1 byte).
	 * @param command [LcdCommand]
	 * @see MessageKind.LCD
	 */
	fun sendLcdCommand(num: Int, command: LcdCommand) = sendBytes(MessageKind.LCD, num, 0xFF, command.code)

	/**
	 * Send LCD line content.
	 *
	 * @param num LCD number (1 byte).
	 * @param line Line number (1 byte).
	 * @param content Content (up to 20 bytes).
	 * @param backlight Backlight on or off (1 byte). Default on.
	 * @see MessageKind.LCD
	 */
	fun sendLcdLine(num: Int, line: Int, content: String, backlight: Boolean = true) =
		sendBytes(MessageKind.LCD, num, if (backlight) LcdCommand.BACKLIGHT.code else LcdCommand.NO_BACKLIGHT.code,
			line, content.length, *content.toByteArray(Charsets.UTF_8).map { it.toInt() }.toIntArray())
}