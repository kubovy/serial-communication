package com.poterion.communication.serial.communicator

/**Connection state. */
enum class State {
	/** Device is disconnected. */
	DISCONNECTED,
	/** Devices is currently connecting but not yet connected. No messages can be still send or received from the
	 *  device. */
	CONNECTING,
	/** Device is connected. Message exchange can be performed. */
	CONNECTED,
	/** No message exchange can be guaranteed. The device is currently being disconnected. */
	DISCONNECTING;
}