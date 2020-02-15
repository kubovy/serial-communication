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
package com.poterion.communication.serial

import javafx.beans.property.SimpleStringProperty

@Deprecated("POC")
class RemoteDeviceInfo @JvmOverloads constructor(name: String = "", address: String = "") {

	private val _deviceName = SimpleStringProperty(name)
	private val _deviceAddress = SimpleStringProperty(address)

	var deviceName: String
		get() = _deviceName.get()
		set(value) {
			_deviceName.set(value)
		}

	var deviceAddress: String
		get() = _deviceAddress.get()
		set(value) {
			_deviceAddress.set(value)
		}
}