package com.poterion.communication.serial.payload

/**
 * Device ID state payload
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class DeviceIdState(val code: Int) {
	CAPABILITIES(0x00),
	NAME(0x01)
}