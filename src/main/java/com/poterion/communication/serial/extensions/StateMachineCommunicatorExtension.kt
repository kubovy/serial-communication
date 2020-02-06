@file:Suppress("unused")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.*
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.listeners.StateMachineCommunicatorListener
import com.poterion.communication.serial.listeners.TempCommunicatorListener

/**
 * Temperature & humidity communicator extension extends the default [Communicator] with [MessageKind.TEMP]
 * capabilities. To catch also incoming messages [register] a [TempCommunicatorListener].
 *
 * @see MessageKind.TEMP
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class StateMachineCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		when {
			messageKind == MessageKind.SM_INPUT && message.size > 4 -> {
				listeners
					.filterIsInstance<StateMachineCommunicatorListener>()
					.forEach {
						it.onStateMachineInputReceived(channel, message[2],
							message.copyOfRange(4, message.size).toString(Charsets.UTF_8))
					}
			}
		}
	}

	/**
	 * Send state.
	 *
	 * @param states List of device-value pairs representing states.
	 * @see MessageKind.SM_STATE_ACTION
	 */
	fun sendAction(states: List<Pair<Int, IntArray>>) = sendBytes(MessageKind.SM_STATE_ACTION, states.size,
		*states.flatMap { (device, value) -> listOf(device, value.size, *value.toTypedArray()) }.toIntArray())
}