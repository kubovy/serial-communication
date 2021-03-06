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

/**
 * RGB light patterns.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class RgbLightPattern(val code: Int,
						   val delay: Int?,
						   val width: Int?,
						   val fading: Int?,
						   val min: Int?,
						   val max: Int?,
						   val timeout: Int?) {
	OFF(0x00, null, null, null, null, null, null),
	LIGHT(0x01, 1000, null, null, null, 255, 50),
	BLINK(0x02, 500, null, null, 0, 255, 3),
	FADE_IN(0x03, 200, null, null, 0, 255, 3),
	FADE_OUT(0x04, 200, null, null, 0, 255, 3),
	FADE_INOUT(0x05, 100, null, null, 0, 255, 3),
	FADE_TOGGLE(0x06, 100, null, null, 0, 255, 3),
	ROTATION(0x07, 500, 10, 24, 0, 255, 3),
	WIPE(0x08, 500, null, 0, 0, 255, 1),
	LIGHTHOUSE(0x09, 750, 5, 32, 0, 255, 3),
	CHAISE(0x0A, 300, 10, 24, 0, 255, 3),
	THEATER(0x0B, 1000, 3, 0, 128, 255, 3)
}