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
 * RGB indicators configuration.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class RgbIndicatorConfiguration(val pattern: RgbPattern = RgbPattern.OFF,
									 @field:JsonProperty("color") private val _color: String = "#000000",
									 val delay: Int = 1000,
									 val minimum: Int = 0,
									 val maximum: Int = 255) {

	constructor(pattern: RgbPattern, color: RgbColor, delay: Int, minimum: Int, maximum: Int) :
			this(pattern, color.toHex(), delay, minimum, maximum)

	val color: RgbColor
		@JsonIgnore get() = _color.toRGBColor() ?: RgbColor()
}