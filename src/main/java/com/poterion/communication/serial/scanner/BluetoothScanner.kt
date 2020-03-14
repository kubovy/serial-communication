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
package com.poterion.communication.serial.scanner

import com.poterion.communication.serial.communicator.BluetoothCommunicator
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.bluetooth.*
import kotlin.concurrent.withLock


/**
 * Bluetooth device scanner.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
object BluetoothScanner : Scanner<BluetoothCommunicator.Descriptor>() {
    private val LOGGER = LoggerFactory.getLogger(BluetoothScanner::class.java)
    private val lock = ReentrantLock()
    private val inquiryCompletedEvent = lock.newCondition()
    private val devicesDiscovered = mutableListOf<BluetoothCommunicator.Descriptor>()
    private var discover: Boolean = true

    private val listener: DiscoveryListener = object : DiscoveryListener {
        override fun deviceDiscovered(btDevice: RemoteDevice, cod: DeviceClass) {
            val device = btDevice.toDescriptor()
            LOGGER.info("${device.name ?: "Unknown Device"} [${device.address} found")
        }

        override fun inquiryCompleted(discType: Int) {
            LOGGER.info("Device Inquiry completed!")
            lock.withLock { inquiryCompletedEvent.signalAll() }
        }

        override fun serviceSearchCompleted(transID: Int, respCode: Int) {
            LOGGER.info("TransID: ${transID}, RespCode: ${respCode}")
        }

        override fun servicesDiscovered(transID: Int, servRecord: Array<ServiceRecord>) {
            val services = servRecord.joinToString("; ") { record ->
                val attributes = record.attributeIDs.map { it to record.getAttributeValue(it) }
                        .joinToString(", ") { (k, v) -> "${k}=${v}" }
                val name = try {
                    record.hostDevice.getFriendlyName(false)
                } catch (e: IOException) {
                    "Unknown Device"
                }
                "${name} [${record.hostDevice.bluetoothAddress}]: ${attributes}"
            }
            LOGGER.info("TransID: ${transID}, ServRecords: ${services}")
        }
    }

    fun scan() {
        discover = true
    }

    override fun getAvailableDevices(): Collection<BluetoothCommunicator.Descriptor>? {
        for (type in listOf(DiscoveryAgent.PREKNOWN, DiscoveryAgent.CACHED)) {
            try {
                LocalDevice
                        .getLocalDevice()
                        .discoveryAgent
                        .retrieveDevices(type)
                        ?.forEach { it.toDescriptor() }
            } catch (e: Throwable) {
                LOGGER.warn(e.message)
            }
        }
        if (discover) try {
            val started = LocalDevice.getLocalDevice().discoveryAgent.startInquiry(DiscoveryAgent.GIAC, listener)
            if (started) {
                LOGGER.info("Wait for device inquiry to complete...")
                lock.withLock { inquiryCompletedEvent.await() }
                LOGGER.info("${devicesDiscovered.size} device(s) found")
                discover = false
                return devicesDiscovered
            }
        } catch (e: Throwable) {
            LOGGER.warn(e.message)
        }
        return devicesDiscovered
    }

    private fun RemoteDevice.toDescriptor(): BluetoothCommunicator.Descriptor {
        val address = bluetoothAddress.chunked(2).joinToString(":")
        val name = try {
            getFriendlyName(false)
        } catch (e: IOException) {
            null
        }
        val device = devicesDiscovered
                .find { it.address == address }
                ?: BluetoothCommunicator.Descriptor(address = address, channel = 6).also { devicesDiscovered.add(it) }
        device.name = name
        return device
    }
}