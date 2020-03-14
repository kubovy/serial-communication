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

import com.poterion.communication.serial.communicator.Channel
import javafx.application.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Device scanner.
 *
 * @author Jan Kubovy <jan@kubovy.eu>
 */
abstract class Scanner<ConnectionDescriptor> {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Scanner::class.java)
    }

    private val scannerExecutor = Executors.newSingleThreadExecutor()
    private val scannerThread: Thread
    private val devices = mutableListOf<ConnectionDescriptor>()
    private val listeners = mutableListOf<ScannerListener<ConnectionDescriptor>>()
    var paused: Boolean = false

    private val scannerRunnable: () -> Unit = {
        while (!Thread.interrupted()) {
            if (!paused) try {
                getAvailableDevices()?.also { currentDevices ->
                    if (devices.size != currentDevices.size || devices.any { !currentDevices.contains(it) }) {
                        devices.clear()
                        devices.addAll(currentDevices)
                        listeners.forEach { Platform.runLater { it.onAvailableDevicesChanged(Channel.USB, devices) } }
                    }
                }
            } catch (e: IOException) {
                LOGGER.error(e.message)
            }
            Thread.sleep(1000L)
        }
    }

    init {
        scannerThread = Thread(scannerRunnable)
        scannerThread.name = "scanner"
        scannerExecutor.execute(scannerThread)
    }

    /**
     * Available devices getter.
     *
     * @return Available devices.
     */
    abstract fun getAvailableDevices(): Collection<ConnectionDescriptor>?

    fun shutdown() {
        scannerThread.interrupt()
        scannerExecutor.shutdown()
        scannerExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)
    }

    /**
     * Register a new listener.
     *
     * @param listener Listener to register.
     */
    fun register(listener: ScannerListener<ConnectionDescriptor>) {
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    /**
     * Unregister an existing listener.
     *
     * @param listener Listener to unregister.
     */
    fun unregister(listener: ScannerListener<ConnectionDescriptor>) = listeners.remove(listener)
}