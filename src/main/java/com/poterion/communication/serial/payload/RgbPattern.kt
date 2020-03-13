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
 * RGB patterns.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class RgbPattern(val code: Int, val delay: Int?, val min: Int?, val max: Int?, val timeout: Int?) {
	/** Off */
	OFF(0x00, null, null, null, null),
	/** Simple light */
	LIGHT(0x01, 1000, null, 255, 1),
	/** Blink 50/50 */
	BLINK(0x02, 500, 0, 255, 3),
	/** Fade in 0>1 */
	FADE_IN(0x03, 200, 0, 255, 3),
	/** Fade out 1>0 */
	FADE_OUT(0x04, 200, 0, 255, 3),
	/** Fade toggle 0>1>0 */
	FADE_INOUT(0x05, 200, 0, 255, 3)
}