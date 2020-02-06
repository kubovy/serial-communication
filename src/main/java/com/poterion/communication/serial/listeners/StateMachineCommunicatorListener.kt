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