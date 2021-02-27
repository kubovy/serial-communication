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
 * Rainbow
 * @param code Byte code
 */
enum class Rainbow(val code: Int) {
	/** No rainbow */
	NO_RAINBOW(0x00),
	/** Default rainbow */
	/** Symmetric rainbow in each row */
	RAINBOW_ROW(0x01),
	/** Symmetric rainbow circle in each row */
	RAINBOW_ROW_CIRCLE(0x02),
	RAINBOW(0x03),
	/** Rainbow circle */
	RAINBOW_CIRCLE(0x04)
}