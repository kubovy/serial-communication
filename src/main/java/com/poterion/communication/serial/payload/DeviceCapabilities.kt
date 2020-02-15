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
 * Device capabilities.
 *
 * @param hasBluetooth Bluetooth connectivity
 * @param hasUSB USB connectivity
 * @param hasTemp Temperature, Humidity sensor
 * @param hasLCD LCD capability
 * @param hasRegistry Registry available
 * @param hasMotionSensor Motion sensor
 * @param hasRgbStrip RGB strip
 * @param hasRgbIndicators RGB LED indicators (e.g. WS281x)
 * @param hasRgbLight RGB LED light (e.g. WS281x)
 * @see com.poterion.communication.serial.MessageKind.IDD
 * @author Jan Kubovy [jan@kubovy.eu]
 */
data class DeviceCapabilities(val hasBluetooth: Boolean = false,
							  val hasUSB: Boolean = false,
							  val hasTemp: Boolean = false,
							  val hasLCD: Boolean = false,
							  val hasRegistry: Boolean = false,
							  val hasMotionSensor: Boolean = false,
							  val hasRgbStrip: Boolean = false,
							  val hasRgbIndicators: Boolean = false,
							  val hasRgbLight: Boolean = false)