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
package com.poterion.communication.serial.communicator

/**
 * Communication channels.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 * @param maxPacketSize Maximum size of a single packet in bytes.
 */
enum class Channel(val maxPacketSize: Int) {
	/**
	 * Universal Serial Bus
	 *
	 * This connection is relatively stable therefore the `maxPacketSize` was chosen to be 256bytes. This is the maximum
	 * base on the microcontroller implementation, where this is the size of the buffer.
	 */
	USB(256),
	/**
	 * Bluetooth
	 *
	 * A wireless connection with lower `maxPacketSize` to compromise between speed and stability.
	 */
	BLUETOOTH(32)
}