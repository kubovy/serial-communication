package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel

/**
 * Registry communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.REGISTRY
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface RegistryCommunicatorListener: CommunicatorListener {
	/**
	 * Registry value callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param address Address.
	 * @param registry Registry.
	 * @param values Values.
	 * @see com.poterion.communication.serial.MessageKind.REGISTRY
	 */
	fun onRegistryValue(channel: Channel, address: Int, registry: Int, vararg values: Int)
}