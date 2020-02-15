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