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
 * Data communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.CONSISTENCY_CHECK
 * @see com.poterion.communication.serial.MessageKind.DATA
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface DataCommunicatorListener: CommunicatorListener {
	/**
	 * Consistency check callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param part Requested data number/ID.
	 * @param checksum Checksum.
	 * @see com.poterion.communication.serial.MessageKind.CONSISTENCY_CHECK
	 */
	fun onConsistencyCheckReceived(channel: Channel, part: Int, checksum: Int)

	/**
	 * Data received callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param part Requested data number/ID.
	 * @param address Address.
	 * @param length Length of data in this part.
	 * @param data Data.
	 * @see com.poterion.communication.serial.MessageKind.DATA
	 */
	fun onDataReceived(channel: Channel, part: Int, address: Int, length: Int, data: IntArray)
}