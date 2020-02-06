@file:Suppress("unused")
package com.poterion.communication.serial.extensions

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.communicator.Communicator
import com.poterion.communication.serial.MessageKind
import com.poterion.communication.serial.listeners.BluetoothCommunicatorListener
import com.poterion.communication.serial.payload.BluetoothPairingMode
import com.poterion.communication.serial.toByteArray
import com.poterion.communication.serial.toString

/**
 * Bluetooth communicator extension extends the default [Communicator] with [MessageKind.BLUETOOTH] capabilities.
 * To catch also incoming messages [register] a [BluetoothCommunicatorListener].
 *
 * @see MessageKind.BLUETOOTH
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class BluetoothCommunicatorExtension<ConnectionDescriptor>(communicator: Communicator<ConnectionDescriptor>) :
	CommunicatorExtension<ConnectionDescriptor>(communicator) {

	override fun onMessageKindReceived(channel: Channel, messageKind: MessageKind, message: IntArray) {
		if (messageKind == MessageKind.BLUETOOTH && message.size == 25) listeners
			.filterIsInstance<BluetoothCommunicatorListener>()
			.forEach {
				it.onBluetoothSettingsUpdated(channel,
					BluetoothPairingMode.values().find { m -> m.code == message[2] } ?: BluetoothPairingMode.JUST_WORK,
					message.copyOfRange(3, 9).toString(Charsets.UTF_8),
					message.copyOfRange(9, 25).toString(Charsets.UTF_8))
			}
	}

	/**
	 * Send Bluetooth settings request.
	 *
	 * @see MessageKind.BLUETOOTH
	 */
	fun sendBluetoothSettingsRequest() = send(MessageKind.BLUETOOTH)

	/**
	 * Send Bluetooth settings.
	 *
	 * @param pairingMode [BluetoothPairingMode].
	 * @param pin PIN code (max 6 digits).
	 * @param name Device name (max 16 characters).
	 * @see MessageKind.BLUETOOTH
	 */
	fun sendBluetoothSettings(pairingMode: BluetoothPairingMode, pin: String, name: String) =
		send(MessageKind.BLUETOOTH, listOf(pairingMode.code).toByteArray() +
				pin.replace("[^\\d]".toRegex(), "").padEnd(6, Char.MIN_VALUE).substring(0, 6).toByteArray(Charsets.UTF_8) +
				name.replace("[^\\w]".toRegex(), "").padEnd(16, Char.MIN_VALUE).substring(0, 16).toByteArray(Charsets.UTF_8))
}