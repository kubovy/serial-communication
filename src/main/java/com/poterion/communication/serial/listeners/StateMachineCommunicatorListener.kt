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
interface StateMachineCommunicatorListener: CommunicatorListener {
	/**
	 * State machine action callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param actions List of device-value pairs representing actions.
	 * @see com.poterion.communication.serial.MessageKind.SM_STATE_ACTION
	 */
	fun onStateMachineActionReceived(channel: Channel, actions: List<Pair<Int, IntArray>>)

	/**
	 * State machine input callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param num Input number.
	 * @param value Input value.
	 * @see com.poterion.communication.serial.MessageKind.SM_STATE_ACTION
	 */
	fun onStateMachineInputReceived(channel: Channel, num: Int, value: String)
}