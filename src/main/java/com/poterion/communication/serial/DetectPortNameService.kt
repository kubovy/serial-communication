package com.poterion.communication.serial

import javafx.concurrent.Service
import javafx.concurrent.Task

/**
 * @author Jan Kubovy [jan@kubovy.eu]
 */
@Deprecated("POC")
class DetectPortNameService : Service<String?>() {
	override fun createTask(): Task<String?> = object : Task<String?>() {
		override fun call(): String? = SerialPortCommunicator.findPort()
	}
}