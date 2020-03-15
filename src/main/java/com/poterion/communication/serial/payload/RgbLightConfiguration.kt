/******************************************************************************
 * Copyright (C) 2020 Jan Kubovy (jan@kubovy.eu)                              *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published      *
 * by the Free Software Foundation, either version 3 of the License, or (at   *
 * your option) any later version.                                            *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU Lesser General Public License for more details.                        *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this program.  If not, see                              *
 * <http://www.gnu.org/licenses/>.                                            *
 ******************************************************************************/
package com.poterion.communication.serial.payload

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.poterion.communication.serial.toHex
import com.poterion.communication.serial.toRGBColor

/**
 * RGB light configuration.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class RgbLightConfiguration(val pattern: RgbLightPattern = RgbLightPattern.OFF,
								 @field:JsonProperty("color1") private val _color1: String = "#000000",
								 @field:JsonProperty("color2") private val _color2: String = "#000000",
								 @field:JsonProperty("color3") private val _color3: String = "#000000",
								 @field:JsonProperty("color4") private val _color4: String = "#000000",
								 @field:JsonProperty("color5") private val _color5: String = "#000000",
								 @field:JsonProperty("color6") private val _color6: String = "#000000",
								 @field:JsonProperty("color7") private val _color7: String = "#000000",
								 val delay: Int = RgbLightPattern.OFF.delay ?: 1000,
								 val width: Int = RgbLightPattern.OFF.width ?: 0,
								 val fading: Int = RgbLightPattern.OFF.fading ?: 0,
								 val minimum: Int = RgbLightPattern.OFF.min ?: 0,
								 val maximum: Int = RgbLightPattern.OFF.max ?: 255,
								 val timeout: Int = RgbLightPattern.OFF.timeout ?: 50,
								 val rainbow: Int = 0x00) {

	constructor(pattern: RgbLightPattern, color1: RgbColor, color2: RgbColor, color3: RgbColor, color4: RgbColor,
				color5: RgbColor, color6: RgbColor, color7: RgbColor, delay: Int, width: Int, fading: Int,
				minimum: Int, maximum: Int, timeout: Int, rainbow: Int) :
			this(pattern, color1.toHex(), color2.toHex(), color3.toHex(), color4.toHex(), color5.toHex(),
					color6.toHex(), color7.toHex(), delay, width, fading, minimum, maximum, timeout, rainbow)

	val color1: RgbColor
		@JsonIgnore get() = _color1.toRGBColor() ?: RgbColor()

	val color2: RgbColor
		@JsonIgnore get() = _color2.toRGBColor() ?: RgbColor()

	val color3: RgbColor
		@JsonIgnore get() = _color3.toRGBColor() ?: RgbColor()

	val color4: RgbColor
		@JsonIgnore get() = _color4.toRGBColor() ?: RgbColor()

	val color5: RgbColor
		@JsonIgnore get() = _color5.toRGBColor() ?: RgbColor()

	val color6: RgbColor
		@JsonIgnore get() = _color6.toRGBColor() ?: RgbColor()

	val color7: RgbColor
		@JsonIgnore get() = _color7.toRGBColor() ?: RgbColor()
}