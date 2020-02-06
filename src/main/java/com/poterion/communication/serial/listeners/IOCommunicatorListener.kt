package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel

/**
 * IO communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.IO
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface IOCommunicatorListener: CommunicatorListener {
	/**
	 * IO state received callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param port IO number.
	 * @param state IO state.
	 * @see com.poterion.communication.serial.MessageKind.IO
	 */
	fun onIoChanged(channel: Channel, port: Int, state: Boolean)
}