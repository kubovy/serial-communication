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

/**
 * Message kind.
 *
 * @param code Code of the message type.
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class MessageKind(val code: Int, val delay: Long? = null) {
	/**
	 * Cyclic redundancy check message
	 *
	 *     |==============|
	 *     | (A) CRC      |
	 *     |--------------|
	 *     |  0 |  1 |  2 |
	 *     | CRC|KIND| CRC|
	 *     |==============|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 */
	CRC(0x00),
	/**
	 * ID of device message
	 *
	 *     |=============================|
	 *     | (A) Ping                    |
	 *     |-----------------------------|
	 *     |  0 |  1 |  2 |              |
	 *     | CRC|KIND| RND|              |
	 *     |=============================|
	 *     | (B) State                   |
	 *     |-----------------------------|
	 *     |  0 |  1 |  2 |  3 |         |
	 *     | CRC|KIND| RND|STAT|         |
	 *     |=============================|
	 *     | (B) Response                |
	 *     |-----------------------------|
	 *     |  0 |  1 |  2 |  3 |  4... 5 |
	 *     | CRC|KIND|0x00|STAT| PAYLOAD |
	 *     |=============================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`RND `**: Random number to force different CRC
	 * - **`STAT`**: Requested state
	 *     - `0x00`: Request capabilities
	 *     - `0x01`: Request device name
	 * - **`PAYLOAD`**: Payload A, depending on `STAT`:
	 *     - `0x00`: Capabilities. Bits:
	 *         - `00`: Bluetooth connectivity
	 *         - `01`: USB connectivity
	 *         - `02`: Temperature, Humidity sensor
	 *         - `03`: LCD capability
	 *         - `04`: Registry available
	 *         - `05`: Motion sensor
	 *         - `06`: _N/A_
	 *         - `07`: _N/A_
	 *         - `08`: RGB strip
	 *         - `09`: RGB LEDs (e.g. WS281x)
	 *         - `10`: RGB LED Light (e.g. WS281x)
	 *         - `11`: _N/A_
	 *         - `12`: _N/A_
	 *         - `13`: _N/A_
	 *         - `14`: _N/A_
	 *         - `15`: _N/A_
	 *     - `0x01`: Device name
	 */
	IDD(0x01),
	/**
	 * Consistency check
	 *
	 *     |========================|
	 *     | (A) Request w/ part    |
	 *     |------------------------|
	 *     |  0 |  1 |  2 |    |    |
	 *     | CRC|KIND|PART|    |    |
	 *     |========================|
	 *     | (B) Response w/ part   |
	 *     |------------------------|
	 *     |  0 |  1 |  2 |  3 |    |
	 *     | CRC|KIND|PART|CKSM|    |
	 *     |========================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`PART`**: Part to check
	 * - **`CKSM`**: State machine checksum
	 */
	CONSISTENCY_CHECK(0x02),
	/**
	 * Data transfer
	 *
	 *     |============================================|
	 *     | (A) Pull                                   |
	 *     |--------------------------------------------|
	 *     |  0 |  1 |  2 |    |    |    |    |    |    |
	 *     | CRC|KIND|PART|    |    |    |    |    |    |
	 *     |============================================|
	 *     | (A) Pull part using starting address       |
	 *     |--------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6 |    |    |
	 *     | CRC|KIND|PART|ADRH|ADRL|LENH|LENL|    |    |
	 *     |============================================|
	 *     | (B) Push                                   |
	 *     |--------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6 |  7...X  |
	 *     | CRC|KIND|PART|ADRH|ADRL|LENH|LENL|   DATA  |
	 *     |============================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`NUM `**: Data number/ID
	 * - **`ADRH`**: Starting address in this packet high byte
	 * - **`ADRL`**: Starting address in this packet low byte
	 * - **`LENH`**: Length high byte
	 * - **`LENL`**: Length low byte
	 * - **`DATA`**: Data till end of the packet
	 */
	DATA(0x03),
	/**
	 * Plain message
	 *
	 *     |===================|
	 *     | (A) Plain message |
	 *     |-------------------|
	 *     |  0 |  1 |  2...LEN|
	 *     | CRC|KIND|   DATA  |
	 *     |===================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`DATA`**: Message
	 */
	PLAIN(0x0F),
	/**
	 * Input/Output message
	 *
	 *     |===================|
	 *     | (A) Request       |
	 *     |-------------------|
	 *     |  0 |  1 |  2 |    |
	 *     | CRC|KIND|PORT|    |
	 *     |===================|
	 *     | (B) Set/Response  |
	 *     |-------------------|
	 *     |  0 |  1 |  2 |  3 |
	 *     | CRC|KIND|PORT| BIT|
	 *     |===================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`PORT`**: IO port number
	 * - **`BIT `**: Bit value, `BIT & 0x80` means setting, otherwise it is a response.
	 */
	IO(0x10),
	/**
	 * Temperature & Humidity sensor message:
	 *
	 *     |==================================|
	 *     | (A) Request sensor count         |
	 *     |----------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |
	 *     |==================================|
	 *     | (B) Response sensor count        |
	 *     |----------------------------------|
	 *     |  0 |  1 |  2 |  3 |    |    |    |
	 *     | CRC|KIND|0xFF| CNT|    |    |    |
	 *     |==================================|
	 *     | (C) Request                      |
	 *     |----------------------------------|
	 *     |  0 |  1 |  2 |    |    |    |    |
	 *     | CRC|KIND| NUM|    |    |    |    |
	 *     |==================================|
	 *     | (D) Response                     |
	 *     |----------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6 |
	 *     | CRC|KIND| NUM| TEMPx100|  RHx100 |
	 *     |==================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`NUM `**: DHT11 number
	 * - **`CNT `**: Count
	 * - **`TEMPx100`**: Temperature in &deg;C multiplied by 100 (`TEMP = TEMPx100 / 100`)
	 * - **`RHx100  `**: Relative humidity in % multiplied by 100 (`RH = RHx100 / 100`)
	 */
	TEMP(0x11),
	/**
	 * LCD display message
	 *
	 *     |=======================================|
	 *     | (A) Request LCD count                 |
	 *     |---------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |    |
	 *     |=======================================|
	 *     | (B) Response LCD count                |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |    |    |    |    |    |
	 *     | CRC|KIND| CNT|    |    |    |    |    |
	 *     |=======================================|
	 *     | (C) Request a line                    |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |  3 |    |    |    |    |
	 *     | CRC|KIND| NUM|LINE|    |    |    |    |
	 *     |=======================================|
	 *     | (D) Send/receive command              |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |    |    |    |
	 *     | CRC|KIND| NUM|0xFF| CMD|    |    |    |
	 *     |=======================================|
	 *     | (E) Set/Response                      |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6...X  |
	 *     | CRC|KIND| NUM| CMD|LINE| LEN|   VAL   |
	 *     |---------------------------------------|
	 *     | X = 6 + LEN                           |
	 *     |=======================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`NUM `**: LCD device num (to support multiple displays
	 * - **`CMD `**:
	 *     - `0x7B` = Clear display (only in _B_)
	 *     - `0x7C` = Reset display (only in _B_)
	 *     - `0x7D` = Backlight
	 *     - `0x7E` = No backlight
	 * - **`LINE`**: Line - `LINE = 0x80` will request all lines. This limits the number of lines to `128`.
	 *               If `LINE >= 0x80` means to request a item on index `LINE - 0x7F` and continue with `LINE + 1`.
	 * - **`LEN `**: Value length
	 * - **`VAL `**: Value
	 *
	 */
	LCD(0x12),
	/**
	 * Registry message
	 *
	 *     |=======================================|
	 *     | (A) Request all registries            |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |    |    |    |    |    |
	 *     | CRC|KIND|ADDR|    |    |    |    |    |
	 *     |=======================================|
	 *     | (B) Request a registry                |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |  3 |    |    |    |    |
	 *     | CRC|KIND|ADDR| REG|    |    |    |    |
	 *     |=======================================|
	 *     | (C) Response/Write a registry         |
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |    |    |    |
	 *     | CRC|KIND|ADDR| REG| VAL|    |    |    |
	 *     |=======================================|
	 *     | (D) Response/Write multiple registries|
	 *     |---------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5  ... CNT  |
	 *     | CRC|KIND|ADDR| REG| CNT| VAL1...VALx  |
	 *     |=======================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`ADDR`**: SPI/I2C address
	 * - **`REG `**: (Starting) Registry.
	 * - **`CNT `**: Registry count
	 * - **`VAL `**: Value
	 */
	REGISTRY(0x13),
	/**
	 * RGB LED strip
	 *
	 *     |=====================================================================|
	 *     | (A) Request strip count                                             |
	 *     |---------------------------------------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |    |    |    |    |    |    |    |
	 *     |=====================================================================|
	 *     | (B) Response strip count                                            |
	 *     |---------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|SCNT|    |    |    |    |    |    |    |    |    |    |    |
	 *     |=====================================================================|
	 *     | (C) Request a configuration item of a strip                         |
	 *     |---------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND| NUM| IDX|    |    |    |    |    |    |    |    |    |    |
	 *     |=====================================================================|
	 *     | (D) Set configuration of a strip                                    |
	 *     |---------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6 |  7...8  |  9 |  10| 11 |    |    |
	 *     | CRC|KIND| NUM|PATN|  R |  G |  B |  DELAY  | MIN| MAX|TOUT|    |    |
	 *     |=====================================================================|
	 *     | (E) Response                                                        |
	 *     |---------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6 |  7 |  8 |  9...10 | 11 | 12 | 13 |
	 *     | CRC|KIND| NUM| CNT| IDX|PATN|  R |  G |  B |  DELAY  | MIN| MAX|TOUT|
	 *     |=====================================================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`SCNT`**: Strip count
	 * - **`NUM `**: Strip number
	 * - **`CNT `**: Configuration count.
	 * - **`IDX `**: Index of item to request. `IDX & 0x80` will request all items. This limits the number of items
	 *               in a list to `128`. `IDX > 0x80` means to request a item on index `IDX - 0x7F` and continue with
	 *               `IDX + 1`.
	 * - **`PATN`**: Animation pattern. `PATN & 0x80` will replace the list with this one item, otherwise a new
	 *               item will be added to the end of the list.
	 *     - `0x00`: Off
	 *     - `0x01`: Light
	 *     - `0x02`: Blink
	 *     - `0x03`: Fade in
	 *     - `0x04`: Fade out
	 *     - `0x05`: Fade in/out
	 * - **`R`,`G`,`B`**: Red, green and blue component of the requested color
	 * - **`DELAY`**: Animation delay (depends on pattern implementation)
	 * - **`MIN`,`MAX`**: Minimum and maximum color values (depends on pattern implementation)
	 * - **`TOUT`**: Number of times to repeat pattern animation before switching to next item in the list
	 */
	RGB(0x15),
	/**
	 * RGB LED Indicators
	 *
	 *     |================================================================|
	 *     | (A) Request strip count                                        |
	 *     |----------------------------------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |    |    |    |    |    |    |
	 *     |================================================================|
	 *     | (B) Response strip count                                       |
	 *     |----------------------------------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|SCNT|    |    |    |    |    |    |    |    |    |    |
	 *     |================================================================|
	 *     | (C) Request an LED                                             |
	 *     |----------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND| NUM| LED|    |    |    |    |    |    |    |    |    |
	 *     |================================================================|
	 *     | (D) Set all LEDs                                               |
	 *     |----------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |    |    |    |    |    |    |    |
	 *     | CRC|KIND| NUM|  R |  G |  B |    |    |    |    |    |    |    |
	 *     |================================================================|
	 *     | (E) Configuration for concrete strip and LED                   |
	 *     |----------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |   4|  5 |  6 |  7 |  8 |  9 | 10 | 11 |    |
	 *     | CRC|KIND| NUM| LED|PATN|  R |  G |  B |  DELAY  | MIN| MAX|    |
	 *     |================================================================|
	 *     | (F) Response                                                   |
	 *     |----------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |  6 |  7 |  8 |  9 | 10 | 11 | 12 |
	 *     | CRC|KIND| NUM| CNT| LED|PATN|  R |  G |  B |  DELAY  | MIN| MAX|
	 *     |================================================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`SCNT`**: Strip count
	 * - **`NUM `**: Strip number
	 * - **`CNT `**: LED count in the strip
	 * - **`LED `**: LED index. `LED & 0x80` will request all LEDs. This limits the number of LEDs  to `128`.
	 *               `LED > 0x80` means to request a LED on index `LED - 0x7F` and continue with `LED + 1`.
	 * - **`PATN`**: Animation pattern.
	 *     - `0x00`: Off
	 *     - `0x01`: Light
	 *     - `0x02`: Blink
	 *     - `0x03`: Fade in
	 *     - `0x04`: Fade out
	 *     - `0x05`: Fade in/out
	 * - **`R`,`G`,`B`**: LED color
	 * - **`DELAY`**: Animation delay (depends on pattern implementation)
	 * - **`MIN`,`MAX`**: Minimum and maximum color values (depends on pattern implementation)
	 */
	INDICATORS(0x16),
	/**
	 * RGB LED Light
	 *
	 *     |===============================================================================|
	 *     | (A) Request light count                                                       |
	 *     |-------------------------------------------------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |    |    |    |    |    |    |    |    |    |
	 *     |===============================================================================|
	 *     | (B) Response light count                                                      |
	 *     |-------------------------------------------------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|SCNT|    |    |    |    |    |    |    |    |    |    |    |    |    |
	 *     |===============================================================================|
	 *     | (C) Request an configuration item of a light                                  |
	 *     |-------------------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |    |    |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND| NUM| IDX|    |    |    |    |    |    |    |    |    |    |    |    |
	 *     |===============================================================================|
	 *     | (D) Add/set configuration for concrete strip NUM                              |
	 *     |-------------------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |    4 ...  24 | 25 | 26 | 27 | 28 | 29 | 30 | 31 |    |    |
	 *     | CRC|KIND| NUM|PATN|  RGB0...RGB6 |  DELAY  |  W | FAD| MIN| MAX|TOUT|    |    |
	 *     |===============================================================================|
	 *     | (E) Response                                                                  |
	 *     |-------------------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |  5 |    6 ...  26 | 27 | 28 | 29 | 30 | 31 | 32 | 33 |
	 *     | CRC|KIND| NUM| CNT| IDX|PATN|  RGB0...RGB6 |  DELAY  |  W | FAD| MIN| MAX|TOUT|
	 *     |===============================================================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`SCNT`**: Strip count
	 * - **`NUM `**: Strip number
	 * - **`CNT `**: Item count
	 * - **`IDX `**: Index of item to request. In (A) `IDX & 0x80` will request all items. This limits the number of
	 *               items in a list to `128`. `IDX > 0x80` means to request a item on index `IDX - 0x7F` and continue
	 *               with `IDX + 1`.
	 * - **`PATN`**: Animation pattern. `PATN & 0x80` will replace the list with this one item, otherwise a new
	 *               item will be added to the end of the list.
	 *     - `0x00`: Off
	 *     - `0x01`: Light
	 *     - `0x02`: Blink
	 *     - `0x03`: Fade in
	 *     - `0x04`: Fade out
	 *     - `0x05`: Fade in/out
	 *     - `0x06`: Fade toggle
	 *     - `0x07`: Rotation
	 *     - `0x08`: Wipe
	 *     - `0x09`: Lighthouse
	 *     - `0x0A`: Chaise
	 *     - `0x0B`: Theater
	 * - **`RGBx`**: 7 colors (`x = 0..6`), each consists of 3 bytes in the `R`, `G`, `B` order (`7 * 3 = 21 bytes`)
	 * - **`DELAY`**: Animation delay (depends on pattern implementation)
	 * - **`W   `**: Animation width (depends on pattern implementation)
	 * - **`FAD `**: Animation fading  (depends on pattern implementation)
	 * - **`MIN`,`MAX`**: Minimum and maximum color values (depends on pattern implementation)
	 * - **`TOUT`**: Number of times to repeat pattern animation before switching to next item in the list
	 */
	LIGHT(0x17),
	/**
	 * Bluetooth settings
	 *
	 *     |==================================|
	 *     | (A) Request                      |
	 *     |----------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |
	 *     |==================================|
	 *     | (B) Set/Response                 |
	 *     |----------------------------------|
	 *     |  0 |  1 |  2 |  3...8  |  9...24 |
	 *     | CRC|KIND|PMOD|   PIN   |   NAME  |
	 *     |==================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`PMOD`**: Pairing mode
	 *     - `0x00`: PIN
	 *     - `0x01`: Just Work
	 *     - `0x02`: Passkey
	 *     - `0x03`: User confirmation
	 * - **`PIN `**: PIN code
	 * - **`NAME`**: Device name
	 */
	BLUETOOTH(0x20),
	/**
	 * State machine set state
	 *
	 *     |===========================================================|
	 *     | (A) Get state                                             |
	 *     |-----------------------------------------------------------|
	 *     |  0 |  1 |    |    |    |    |    |    |    |    |    |    |
	 *     | CRC|KIND|    |    |    |    |    |    |    |    |    |    |
	 *     |===========================================================|
	 *     | (A) Execute action                                        |
	 *     |-----------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 | 5...LEN1| .. |    |    |  ...LENx|
	 *     | CRC|KIND| CNT|DEV1|LEN1|  DATA1  | .. |DEVx|LENx|  DATAx  |
	 *     |===========================================================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`CNT `**: State/action count
	 * - **`DEVx`**: Device of state `x`
	 * - **`LENx`**: Length of state `x`
	 * - **`DATAx`**: Data of state `x`
	 */
	SM_STATE_ACTION(0x80),
	/**
	 * State machine input
	 *
	 *     |=============================|
	 *     | (A) Input                   |
	 *     |-----------------------------|
	 *     |  0 |  1 |  2 |  3 |  4...LEN|
	 *     | CRC|KIND| NUM| LEN|  VALUE  |
	 *     |=============================|
	 *
	 * - **`CRC `**: Checksum of the packet
	 * - **`KIND`**: Message kind
	 * - **`NUM `**: Input number
	 * - **`LEN `**: Length
	 * - **`VALUE`**: Value
	 */
	SM_INPUT(0x81),
	/** Debug message */
	DEBUG(0xFE),
	/** Unknown message */
	UNKNOWN(0xFF)
}