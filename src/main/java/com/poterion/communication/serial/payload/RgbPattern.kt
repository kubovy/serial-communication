package com.poterion.communication.serial.payload

/**
 * RGB patterns.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class RgbPattern(val code: Int) {
	/** Off */
	OFF(0x00),
	/** Simple light */
	LIGHT(0x01),
	/** Blink 50/50 */
	BLINK(0x02),
	/** Fade in 0>1 */
	FADE_IN(0x03),
	/** Fade out 1>0 */
	FADE_OUT(0x04),
	/** Fade toggle 0>1>0 */
	FADE_INOUT(0x05)
}