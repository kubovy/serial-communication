@file:Suppress("unused")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.*
import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.listeners.TempCommunicatorListener

/**
 * Temperature & humidity communicator extension extends the default [Communicator] with [MessageKind.TEMP]
 * capabilities. To catch also incoming messages [register] a [TempCommunicatorListener].
 *
 * @see MessageKind.TEMP
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class TempCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.TEMP) when {
			message.size == 4 && message[2] == 0xFF -> listeners.filterIsInstance<TempCommunicatorListener>().forEach {
				it.onTempCountReceived(channel, message[3])
			}
			message.size == 7 -> listeners.filterIsInstance<TempCommunicatorListener>().forEach {
				it.onTempReceived(channel, message[2],
					(message[3] to message[4]).toDoubleInt().toDouble() / 100.0,
					(message[5] to message[6]).toDoubleInt().toDouble() / 100.0)
			}
		}
	}

	/**
	 * Request number of temperature/humidity sensors.
	 *
	 * @see MessageKind.TEMP
	 */
	fun sendTempCountRequest() = send(MessageKind.TEMP)

	/**
	 * Request temperature/humidity.
	 *
	 * @param num Sensor number (1 byte).
	 * @see MessageKind.TEMP
	 */
	fun sendTempRequest(num: Int) = sendBytes(MessageKind.TEMP, num)
}