package com.poterion.communication.serial.payload

/**
 * Bluetooth pairing modes.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class BluetoothPairingMode(val code: Int) {
	PIN(0x00),
	JUST_WORK(0x01),
	PASSKEY(0x02),
	USER_CONFIRMATION(0x03)
}