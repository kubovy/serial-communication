package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel
import com.poterion.communication.serial.payload.BluetoothPairingMode

/**
 * Bluetooth communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.BLUETOOTH
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface BluetoothCommunicatorListener: CommunicatorListener {
	/**
	 * Bluetooth settings update callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param pairingMode [BluetoothPairingMode].
	 * @param pin PIN code.
	 * @param name Device name.
	 * @see com.poterion.communication.serial.MessageKind.BLUETOOTH
	 */
	fun onBluetoothSettingsUpdated(channel: Channel, pairingMode: BluetoothPairingMode, pin: String, name: String)
}