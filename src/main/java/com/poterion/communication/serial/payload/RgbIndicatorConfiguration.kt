package com.poterion.communication.serial.payload

import java.awt.Color

/**
 * RGB indicators configuration.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class RgbIndicatorConfiguration(val pattern: RgbPattern,
									 val color: Color,
									 val delay: Int,
									 val minimum: Int,
									 val maximum: Int)