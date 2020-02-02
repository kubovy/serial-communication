package com.poterion.communication.serial

/**
 * Message kind.
 *
 * @param code Code of the message type.
 * @author Jan Kubovy [jan@kubovy.eu]
 */
enum class MessageKind(val code: Int, val delay: Long? = null) {
	/**
	 * Cyclic redundancy check message:
	 *
	 *     |==============|
	 *     | (A) CRC      |
	 *     |--------------|
	 *     |  0 |  1 |  2 |
	 *     | CRC|KIND| CRC|
	 *     |==============|
	 *
	 * - **CRC** : Checksum of the packet
	 * - **KIND**: Message kind
	 */
	CRC(0x00),
	/**
	 * ID of device message.
	 *
	 *     |====================|
	 *     | (A) Ping           |
	 *     |--------------------|
	 *     |  0 |  1 |  2 |     |
	 *     | CRC|KIND| RND|     |
	 *     |====================|
	 *     | (B) State          |
	 *     |--------------------|
	 *     |  0 |  1 |  2 |   3 |
	 *     | CRC|KIND| RND|STATE|
	 *     |====================|
	 *
	 * - **CRC**  : Checksum of the packet
	 * - **KIND** : Message kind
	 * - **RND**  : Random number
	 * - **STATE**: State
	 */
	IDD(0x01),
	/**
	 * Plain message
	 *
	 *     |===========================|
	 *     | (A) Request concrete item |
	 *     |---------------------------|
	 *     |  0 |  1 |  2 |  3..LEN|   |
	 *     | CRC|KIND| LEN| MESSAGE|   |
	 *     |===========================|
	 *
	 * - **CRC**    : Checksum of the packet
	 * - **KIND**   : Message kind
	 * - **LEN**    : Length
	 * - **MESSAGE**: Message
	 */
	PLAIN(0x02),
	/**
	 * Input/Output message
	 *
	 *     |==================================|
	 *     | (A) Request concrete item        |
	 *     |----------------------------------|
	 *     |  0 |  1 |  2 |                   |
	 *     | CRC|KIND| NUM|                   |
	 *     |==================================|
	 *     | (B) Concrete item (set/response) |
	 *     |----------------------------------|
	 *     |  0 |  1 |  2 |  3 |              |
	 *     | CRC|KIND| NUM| BIT|              |
	 *     |==================================|
	 *
	 * - **CRC** : Checksum of the packet
	 * - **KIND**: Message kind
	 * - **NUM** : GPIO number
	 * - **BIT** : Bit value
	 */
	IO(0x10),
	/**
	 * DHT11 Temperature & Humidity sensor message:
	 *
	 *     |------------------------------------------------|
	 *     | (A) Request for all items of default strip     |
	 *     |------------------------------------------------|
	 *     |  0 |  1 |                                      |
	 *     | CRC|KIND|                                      |
	 *     |================================================|
	 *     | (B) Request concrete item of default strip     |
	 *     |------------------------------------------------|
	 *     |  0 |  1 |  2 |                                 |
	 *     | CRC|KIND| NUM|                                 |
	 *     |================================================|
	 *     | (C) Response                                   |
	 *     |------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |   5    |              |
	 *     | CRC|KIND| NUM| TEMPx10 |HUMIDITY|              |
	 *     |================================================|
	 *
	 * - **CRC**     : Checksum of the packet
	 * - **KIND**    : Message kind
	 * - **NUM**     : DHT11 number
	 * - **TEMPx10** : Temperature in &deg;C multiplied by 10 (`TEMP = TEMPx10 / 10`)
	 * - **HUMIDITY**: Humidity in %
	 */
	DHT11(0x11),
	/**
	 * LCD display message
	 *
	 *     |==========================================|
	 *     | (A) Request all lines                    |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |                           |
	 *     | CRC|KIND| NUM|                           |
	 *     |==========================================|
	 *     | (B) Clear display                        |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |                      |
	 *     | CRC|KIND| NUM|0x7B|                      |
	 *     |==========================================|
	 *     | (C) Reset LCD                            |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |                      |
	 *     | CRC|KIND| NUM|0x7C|                      |
	 *     |==========================================|
	 *     | (D) Turn backlight on/off                |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |       3 |                 |
	 *     | CRC|KIND| NUM|BACKLIGHT|                 |
	 *     |==========================================|
	 *     | (E) Request a concrete line              |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |                      |
	 *     | CRC|KIND| NUM|LINE|                      |
	 *     |==========================================|
	 *     | (F) Concrete line                        |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |       3 |  4 |  5 | 6..LEN|
	 *     | CRC|KIND| NUM|BACKLIGHT|LINE| LEN|MESSAGE|
	 *     |==========================================|
	 *
	 * - **CRC**      : Checksum of the packet
	 * - **KIND**     : Message kind
	 * - **NUM**      : LCD device num (to support multiple displays
	 * - **BACKLIGHT**: `0x7D` = Backlight, `0x7E` = No backlight
	 * - **LINE**     : Line. (`LINE & 0x80`) will request all lines. This limits the number of lines to `128`.
	 *                  `LINE > 0x80` means to request a item on index (`LINE & 0x7F`) and continue with
	 *                  (`LINE + 1`).
	 * - **LEN**      : Length
	 * - **MESSAGE**  : Message
	 */
	LCD(0x12),
	/**
	 * MCP23017 message:
	 *
	 *     |==========================================|
	 *     | (A) Request                              |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |                           |
	 *     | CRC|KIND|ADDR|                           |
	 *     |==========================================|
	 *     | (B) Response                             |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |   3 |   4 |               |
	 *     | CRC|KIND|ADDR|GPIOA|GPIOB|               |
	 *     |==========================================|
	 *     | (C) Write 1 port                         |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |                 |
	 *     | CRC|KIND|ADDR|PORT|GPIO|                 |
	 *     |==========================================|
	 *     | (D) Write both ports                     |
	 *     |------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4  |   5 |          |
	 *     | CRC|KIND|ADDR|0x03|GPIOA|GPIOB|          |
	 *     |==========================================|
	 *
	 * - **CRC**  : Checksum of the packet
	 * - **KIND** : Message kind
	 * - **ADDR** : I2C address
	 * - **PORT** : `0x01` - port A, `0x02` - port B, `0x03` - port A and port B
	 * - **GPIO** : GPIO value
	 * - **GPIOA**: GPIO A value
	 * - **GPIOB**: GPIO B value
	 */
	MCP23017(0x13),
	/**
	 * PIR message:
	 *
	 *     |==========================================|
	 *     | (A) Request                              |
	 *     |------------------------------------------|
	 *     |  0 |  1 |                                |
	 *     | CRC|KIND|                                |
	 *     |==========================================|
	 *     | (B) Response                             |
	 *     |------------------------------------------|
	 *     |  0 |  1 |   2 |                          |
	 *     | CRC|KIND|VALUE|                          |
	 *     |==========================================|
	 *
	 * - **CRC**  : Checksum of the packet
	 * - **KIND** : Message kind
	 * - **VALUE**: Value
	 */
	PIR(0x14),
	/**
	 * RGB LED Strip:
	 *
	 *     |=======================================================================|
	 *     | (A) Request concrete item of a concrete strip                         |
	 *     |-----------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |                                                   |
	 *     | CRC|KIND| NUM| IDX|                                                   |
	 *     |=======================================================================|
	 *     | (B) Configuration for default strip                                   |
	 *     |-----------------------------------------------------------------------|
	 *     |  0 |  1 |   2   |  3 |  4 |  5 |6...7|  8 |  9 |   10  |              |
	 *     | CRC|KIND|PATTERN|  R |  G |  B |DELAY| MIN| MAX|TIMEOUT|              |
	 *     |=======================================================================|
	 *     | (C) Configuration for concrete strip NUM                              |
	 *     |-----------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |   3   |  4 |  5 |  6 |7...8|  9 |  10|   11  |         |
	 *     | CRC|KIND| NUM|PATTERN|  R |  G |  B |DELAY| MIN| MAX|TIMEOUT|         |
	 *     |=======================================================================|
	 *     | (D) Response                                                          |
	 *     |-----------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |  4 |   5   |  6 |  7 |  8 |9..10| 11 | 12 |   13  |
	 *     | CRC|KIND| NUM| CNT| IDX|PATTERN|  R |  G |  B |DELAY| MIN| MAX|TIMEOUT|
	 *     |=======================================================================|
	 *
	 * - **CRC**    : Checksum of the packet
	 * - **KIND**   : Message kind
	 * - **NUM**    : Strip number
	 * - **IDX**    : Index of item to request. (`IDX & 0x80`) will request all items. This limits the number of items
	 *                in a list to `128`. `IDX > 0x80` means to request a item on index (`IDX & 0x7F`) and continue with
	 *                (`IDX + 1`).
	 * - **PATTERN**: The requested pattern. (`PATTERN & 0x80` will replace the list with this one item)
	 * - **R,G,B**  : Reg, green and blue component of the requested color
	 * - **DELAY**  : Animation delay (depends on pattern implementation)
	 * - **MIN,MAX**: Minimum and maximum color values (depends on pattern implementation)
	 * - **TIMEOUT**: Number of times to repeat pattern animation before switching to next item in the list
	 */
	RGB(0x15),
	/**
	 * WS281x RGB Light:
	 *
	 *     |=================================================================================|
	 *     | (A) Request concrete item of a concrete strip                                   |
	 *     |---------------------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |  3 |                                                             |
	 *     | CRC|KIND| NUM| IDX|                                                             |
	 *     |=================================================================================|
	 *     | (B) Configuration for default strip                                             |
	 *     |---------------------------------------------------------------------------------|
	 *     |  0 |  1 |   2   | 3  ... 23 |24 25|  26 |  27  | 28| 29|   30  |                |
	 *     | CRC|KIND|PATTERN|RGB0...RGB7|DELAY|WIDTH|FADING|MIN|MAX|TIMEOUT|                |
	 *     |=================================================================================|
	 *     | (C) Configuration for concrete strip NUM                                        |
	 *     |---------------------------------------------------------------------------------|
	 *     |  0 |  1 |  2 |   3   | 4  ... 24 |25 26|  27 |  28  | 29| 30|   31  |           |
	 *     | CRC|KIND| NUM|PATTERN|RGB0...RGB7|DELAY|WIDTH|FADING|MIN|MAX|TIMEOUT|           |
	 *     |=================================================================================|
	 *     | (D) Response                                                                    |
	 *     |  0 |  1 |  2 |  3 |  4 |   5   |   6...26  |27 28|  29 |  30  | 31 | 32 |   33  |
	 *     | CRC|KIND| NUM| CNT| IDX|PATTERN|RGB0...RGB7|DELAY|WIDTH|FADING| MIN| MAX|TIMEOUT|
	 *     |=================================================================================|
	 *
	 * - **CRC**    : Checksum of the packet
	 * - **KIND**   : Message kind
	 * - **NUM**    : Strip number
	 * - **IDX**    : Index of item to request. (`IDX & 0x80`) will request all items. This limits the number of items
	 *                in a list to `128`. `IDX > 0x80` means to request a item on index (`IDX & 0x7F`) and continue with
	 *                (`IDX + 1`)
	 * - **PATTERN**: The requested pattern. (`PATTERN & 0x80` will replace the list with this one item)
	 * - **RGB0-7** : 8 colors, each consists of 3 bytes in the R, G, B order
	 * - **DELAY**  : Animation delay (depends on pattern implementation)
	 * - **WIDTH**  : Animation width (depends on pattern implementation)
	 * - **FADING** : Animation fading  (depends on pattern implementation)
	 * - **MIN,MAX**: Minimum and maximum color values (depends on pattern implementation)
	 * - **TIMEOUT**: Number of times to repeat pattern animation before switching to next item in the list
	 */
	WS281x(0x16),
	/**
	 * @see WS281x
	 */
	WS281xLIGHT(0x17),
	BT_SETTINGS(0x20),
	BT_EEPROM(0x21),
	SM_CONFIGURATION(0x80),
	SM_PULL(0x81),
	SM_PUSH(0x82),
	SM_GET_STATE(0x83),
	SM_SET_STATE(0x84),
	SM_ACTION(0x85),
	SM_INPUT(0x86),
	DEBUG(0xFE),
	/** Unknown message */
	UNKNOWN(0xFF)
}