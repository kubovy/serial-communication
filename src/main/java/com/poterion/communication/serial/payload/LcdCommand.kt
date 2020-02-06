@file:Suppress("unused")
package com.poterion.communication.serial.payload

/**
 * LCD commands.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class LcdCommand(val code: Int) {
    CLEAR(0x7B),
    RESET(0x7C),
    BACKLIGHT(0x7D),
    NO_BACKLIGHT(0x7E)
}