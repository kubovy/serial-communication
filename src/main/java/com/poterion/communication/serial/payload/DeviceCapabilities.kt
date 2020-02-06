package com.poterion.communication.serial.payload

/**
 * Device capabilities.
 *
 * @param hasBluetooth Bluetooth connectivity
 * @param hasUSB USB connectivity
 * @param hasTemp Temperature, Humidity sensor
 * @param hasLCD LCD capability
 * @param hasRegistry Registry available
 * @param hasMotionSensor Motion sensor
 * @param hasRgbStrip RGB strip
 * @param hasRgbIndicators RGB LED indicators (e.g. WS281x)
 * @param hasRgbLight RGB LED light (e.g. WS281x)
 * @see com.poterion.communication.serial.MessageKind.IDD
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class DeviceCapabilities(val hasBluetooth: Boolean = false,
							  val hasUSB: Boolean = false,
							  val hasTemp: Boolean = false,
							  val hasLCD: Boolean = false,
							  val hasRegistry: Boolean = false,
							  val hasMotionSensor: Boolean = false,
							  val hasRgbStrip: Boolean = false,
							  val hasRgbIndicators: Boolean = false,
							  val hasRgbLight: Boolean = false)