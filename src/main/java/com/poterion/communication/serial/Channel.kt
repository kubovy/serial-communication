package com.poterion.communication.serial

/**
 * Communication channels.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 * @param maxPacketSize Maximum size of a single packet in bytes.
 */
enum class Channel(val maxPacketSize: Int) {
	/**
	 * Universal Serial Bus
	 *
	 * This connection is relatively stable therefore the `maxPacketSize` was chosen to be 256bytes. This is the maximum
	 * base on the microcontroller implementation, where this is the size of the buffer.
	 */
	USB(256),
	/**
	 * Bluetooth
	 *
	 * A wireless connection with lower `maxPacketSize` to compromise between speed and stability.
	 */
	BLUETOOTH(32)
}