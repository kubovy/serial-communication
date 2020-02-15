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