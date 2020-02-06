package com.poterion.communication.serial.listeners

import com.poterion.communication.serial.communicator.Channel

/**
 * Data communicator extension listener.
 *
 * @see com.poterion.communication.serial.MessageKind.CONSISTENCY_CHECK
 * @see com.poterion.communication.serial.MessageKind.DATA
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface DataCommunicatorListener: CommunicatorListener {
	/**
	 * Consistency check callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param part Requested data number/ID.
	 * @param checksum Checksum.
	 * @see com.poterion.communication.serial.MessageKind.CONSISTENCY_CHECK
	 */
	fun onConsistencyCheckReceived(channel: Channel, part: Int, checksum: Int)

	/**
	 * Data received callback.
	 *
	 * @param channel Channel triggering the event.
	 * @param part Requested data number/ID.
	 * @param address Address.
	 * @param length Length of data in this part.
	 * @param data Data.
	 * @see com.poterion.communication.serial.MessageKind.DATA
	 */
	fun onDataReceived(channel: Channel, part: Int, address: Int, length: Int, data: IntArray)
}