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