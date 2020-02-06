package com.poterion.communication.serial.payload

import java.awt.Color

/**
 * RGB light configuration.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class RgbLightConfiguration(val pattern: RgbLightPattern,
								 val color1: Color,
								 val color2: Color,
								 val color3: Color,
								 val color4: Color,
								 val color5: Color,
								 val color6: Color,
								 val color7: Color,
								 val delay: Int,
								 val width: Int,
								 val fading: Int,
								 val minimum: Int,
								 val maximum: Int,
								 val timeout: Int)