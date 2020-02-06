package com.poterion.communication.serial.payload

/**
 * RGB light patterns.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class RgbLightPattern(val code: Int) {
	OFF(0x00),
	LIGHT(0x01),
	BLINK(0x02),
	FADE_IN(0x03),
	FADE_OUT(0x04),
	FADE_INOUT(0x05),
	FADE_TOGGLE(0x06),
	ROTATION(0x07),
	WIPE(0x08),
	LIGHTHOUSE(0x09),
	CHAISE(0x0A),
	THEATER(0x0B)
}