package com.poterion.communication.serial.payload

import java.awt.Color

/**
 * RGB strip configuration.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class RgbStripConfiguration(val pattern: RgbPattern,
								 val color: Color,
								 val delay: Int,
								 val minimum: Int,
								 val maximum: Int,
								 val timeout: Int)