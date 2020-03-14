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
@file:Suppress("unused")

package com.poterion.communication.serial

import com.poterion.communication.serial.payload.ColorOrder
import java.awt.Color
import java.nio.charset.Charset

/**
 * Converts 8 bools to one byte.
 *
 * @param b7 Bit 7
 * @param b6 Bit 6
 * @param b5 Bit 5
 * @param b4 Bit 4
 * @param b3 Bit 3
 * @param b2 Bit 2
 * @param b1 Bit 1
 * @param b0 Bit 0
 * @return Composed byte.
 */
fun bools2Byte(b7: Boolean, b6: Boolean, b5: Boolean, b4: Boolean, b3: Boolean, b2: Boolean, b1: Boolean, b0: Boolean) =
	(if (b7) 0b10000000 else 0b00000000) or
			(if (b6) 0b01000000 else 0b00000000) or
			(if (b5) 0b00100000 else 0b00000000) or
			(if (b4) 0b00010000 else 0b00000000) or
			(if (b3) 0b00001000 else 0b00000000) or
			(if (b2) 0b00000100 else 0b00000000) or
			(if (b1) 0b00000010 else 0b00000000) or
			(if (b0) 0b00000001 else 0b00000000)

/**
 * Converts a byte to an list of 8 bools.
 *
 * @param byte Byte to convert.
 * @return List containing 8 bools corresponding to the 8 bits in a byte.
 */
fun byte2Bools(byte: Int) = listOf(
		(byte and 0b00000001) > 0,
		(byte and 0b00000010) > 0,
		(byte and 0b00000100) > 0,
		(byte and 0b00001000) > 0,
		(byte and 0b00010000) > 0,
		(byte and 0b00100000) > 0,
		(byte and 0b01000000) > 0,
		(byte and 0b10000000) > 0)

/**
 * Calculates a checksum.
 */
fun ByteArray.calculateChecksum() =
	(map { it.toInt() }.takeIf { it.isNotEmpty() }?.reduce { acc, i -> (acc + i) and 0xFF } ?: 0) and 0xFF

fun Pair<Byte, Byte>.toInt() = let { first.toInt() to second.toInt() }.toDoubleInt()

fun Pair<Int, Int>.toDoubleInt() = let { ((it.first shl 8) and 0xFF00) or (it.second and 0xFF) }

fun Int.toByteArray() = listOf(this).toByteArray()

fun List<Int>.toByteArray() = this.map { it.toByte() }.toByteArray()

fun IntArray.toString(charset: Charset) = map { it.toByte() }.toByteArray().toString(charset)

fun Color.toComponents(colorOrder: ColorOrder = ColorOrder.RGB) = when (colorOrder) {
	ColorOrder.RGB -> intArrayOf(red, green, blue)
	ColorOrder.GRB -> intArrayOf(green, red, blue)
}

fun IntArray.toColor(colorOrder: ColorOrder = ColorOrder.RGB, from: Int = 0) = when (colorOrder) {
	ColorOrder.RGB -> Color(this[from], this[from + 1], this[from + 2])
	ColorOrder.GRB -> Color(this[from + 1], this[from], this[from + 2])
}