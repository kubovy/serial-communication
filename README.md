Serial communicator enabling to communicated with a peripheral device over a
[Channel](src/main/java/com/poterion/communication/serial/Channel.kt) using binary messages. The first 2 bytes of every
message have a fixed semantic:

     | CRC|KIND|...
     |0xXX|0xYY|...

First byte is a [checksum](src/main/java/com/poterion/communication/serial/Utils.kt) and the second byte is a
[MessageKind](src/main/java/com/poterion/communication/serial/MessageKind.kt). The rest of the message depends on the
implementation of a concrete [MessageKind](src/main/java/com/poterion/communication/serial/MessageKind.kt). The maximum
length of a message is determined by the
[Channel.maxPacketSize](src/main/java/com/poterion/communication/serial/Channel.kt). If necessary a message needs to be
split to different packets.

A communicator manages three threads: connection thread, inbound thread, and outbound thread.

The connection thread in [State.CONNECTING](src/main/java/com/poterion/communication/serial/Communicator.kt) interrupts
both, inbound and outboud, communication threads, if such are alive. Clean up the connection and tries to start new set
of inbound and outbound communication threads. In all other
[states](src/main/java/com/poterion/communication/serial/Communicator.kt) it just sleeps.

The inbound thread waits for [nextMessage](src/main/java/com/poterion/communication/serial/Communicator.kt), which is
expected to be implemented as a blocking function. Only non empty messages are considered.

First byte of that message is extracted as a message-checksum calculated by the sender. Then the rest of the message is
used to [calculate a checksum](src/main/java/com/poterion/communication/serial/Utils.kt) on the receiver side.
If both, the received and the calculated, checksums match, the message is considered as valid for further processing.
Otherwise, the message is discarded.

A [MessageKind](src/main/java/com/poterion/communication/serial/MessageKind.kt) is then determined based on the second
byte. All non-[CRC](src/main/java/com/poterion/communication/serial/MessageKind.kt) messages a CRC checksum is send
back to the sender containing the [calculated checksum](src/main/java/com/poterion/communication/serial/Utils.kt):

      | CRC|KIND|CONTENT|
      |0xXX|0x00|  0xXX |

A [MessageKind.CRC](src/main/java/com/poterion/communication/serial/MessageKind.kt) message will store the received CRC
to compare with the last sent message. This way the communicator determines when a sent message was successfully
received by the other side.

A [MessageKind.IDD](src/main/java/com/poterion/communication/serial/MessageKind.kt) message implements a protocol to
initialize a connection and determine its stability.

Any other [MessageKind](src/main/java/com/poterion/communication/serial/MessageKind.kt) will be simply forwarded using 
the [CommunicatorListener.onMessageReceived](src/main/java/com/poterion/communication/serial/CommunicatorListener.kt)
and has to be implemented further.

Last, the outbound thread monitors 2 queues: the _checksum queue_ and the _message queue_. The _checksum queue_
has priority over the _message queue_. The _checksum queue_ contains a list of
[checksum bytes](src/main/java/com/poterion/communication/serial/Utils.kt) of recently received messages to be
confirmed to the sender. The _message queue_ contains a list of binary messages to be send to the receiver without the
checksum byte (the 1st byte). The checksum will be calculated freshly before any send attempt.

The following flows and situactions are considered during communication between the sender and the receiver. Both
side share this implementation:

In good case transmitting a message works first time and is being confirmed right-away:

![Success](https://github.com/kubovy/serial-communication/raw/master/src/img/communication-success.png)

One typical error case is when a message does not reach its target. In this case the message is resend again after
a certain timeout (by default 500ms -
[MESSAGE_CONFIRMATION_TIMEOUT](src/main/java/com/poterion/communication/serial/Communicator.kt), but can be overwritten
for different [MessageKind](src/main/java/com/poterion/communication/serial/MessageKind.kt) -
[MessageKind.delay](src/main/java/com/poterion/communication/serial/MessageKind.kt)).

![Sent timeout](https://github.com/kubovy/serial-communication/raw/master/src/img/communication-sent-timeout.png)

In the same way it may happen, that the message got received corretly, but the CRC was not. Each
[MessageKind](src/main/java/com/poterion/communication/serial/MessageKind.kt) needs to be implemented in a idempotent
way so receiving multiple same messages after each other should not result in errors.

![CRC timeout](https://github.com/kubovy/serial-communication/raw/master/src/img/communication-confirmation-timeout.png)

To prevent resent starvation, one message is retried up to 20 times by default
([MAX_SEND_ATTEMPTS](src/main/java/com/poterion/communication/serial/Communicator.kt)) and then it is dropped. The
developer is responsible for resolution of such situations.

The connection can be in one of the following states:

 - [State.DISCONNECTED](src/main/java/com/poterion/communication/serial/Communicator.kt)
 - [State.CONNECTING](src/main/java/com/poterion/communication/serial/Communicator.kt)
 - [State.CONNECTED](src/main/java/com/poterion/communication/serial/Communicator.kt)
 - [State.DISCONNECTING](src/main/java/com/poterion/communication/serial/Communicator.kt)


## Message Types and their payload


### [0x00] Cyclic redundancy check message (CRC)

    |==============|
    | (A) CRC      |
    |--------------|
    |  0 |  1 |  2 |
    | CRC|KIND| CRC|
    |==============|

- **CRC** : Checksum of the packet
- **KIND**: Message kind


### [0x01] ID of device message (IDD)

    |====================|
    | (A) Ping           |
    |--------------------|
    |  0 |  1 |  2 |     |
    | CRC|KIND| RND|     |
    |====================|
    | (B) State          |
    |--------------------|
    |  0 |  1 |  2 |   3 |
    | CRC|KIND| RND|STATE|
    |====================|

- **CRC**  : Checksum of the packet
- **KIND** : Message kind
- **RND**  : Random number
- **STATE**: State


### [0x02] Plain message (PLAIN)

    |===========================|
    | (A) Request concrete item |
    |---------------------------|
    |  0 |  1 |  2 |  3..LEN|   |
    | CRC|KIND| LEN| MESSAGE|   |
    |===========================|

- **CRC**    : Checksum of the packet
- **KIND**   : Message kind
- **LEN**    : Length
- **MESSAGE**: Message


### [0x10] Input/Output message (IO)

    |==================================|
    | (A) Request concrete item        |
    |----------------------------------|
    |  0 |  1 |  2 |                   |
    | CRC|KIND| NUM|                   |
    |==================================|
    | (B) Concrete item (set/response) |
    |----------------------------------|
    |  0 |  1 |  2 |  3 |              |
    | CRC|KIND| NUM| BIT|              |
    |==================================|

- **CRC** : Checksum of the packet
- **KIND**: Message kind
- **NUM** : GPIO number
- **BIT** : Bit value


### [0x11] DHT11 Temperature & Humidity sensor message (DHT11)

    |------------------------------------------------|
    | (A) Request for all items of default strip     |
    |------------------------------------------------|
    |  0 |  1 |                                      |
    | CRC|KIND|                                      |
    |================================================|
    | (B) Request concrete item of default strip     |
    |------------------------------------------------|
    |  0 |  1 |  2 |                                 |
    | CRC|KIND| NUM|                                 |
    |================================================|
    | (C) Response                                   |
    |------------------------------------------------|
    |  0 |  1 |  2 |  3 |  4 |   5    |              |
    | CRC|KIND| NUM| TEMPx10 |HUMIDITY|              |
    |================================================|

- **CRC**     : Checksum of the packet
- **KIND**    : Message kind
- **NUM**     : DHT11 number
- **TEMPx10** : Temperature in &deg;C multiplied by 10 (`TEMP = TEMPx10 / 10`)
- **HUMIDITY**: Humidity in %


### [0x12] LCD display message (LCD)

    |==========================================|
    | (A) Request all lines                    |
    |------------------------------------------|
    |  0 |  1 |  2 |                           |
    | CRC|KIND| NUM|                           |
    |==========================================|
    | (B) Clear display                        |
    |------------------------------------------|
    |  0 |  1 |  2 |  3 |                      |
    | CRC|KIND| NUM|0x7B|                      |
    |==========================================|
    | (C) Reset LCD                            |
    |------------------------------------------|
    |  0 |  1 |  2 |  3 |                      |
    | CRC|KIND| NUM|0x7C|                      |
    |==========================================|
    | (D) Turn backlight on/off                |
    |------------------------------------------|
    |  0 |  1 |  2 |       3 |                 |
    | CRC|KIND| NUM|BACKLIGHT|                 |
    |==========================================|
    | (E) Request a concrete line              |
    |------------------------------------------|
    |  0 |  1 |  2 |  3 |                      |
    | CRC|KIND| NUM|LINE|                      |
    |==========================================|
    | (F) Concrete line                        |
    |------------------------------------------|
    |  0 |  1 |  2 |       3 |  4 |  5 | 6..LEN|
    | CRC|KIND| NUM|BACKLIGHT|LINE| LEN|MESSAGE|
    |==========================================|

- **CRC**      : Checksum of the packet
- **KIND**     : Message kind
- **NUM**      : LCD device num (to support multiple displays
- **BACKLIGHT**: `0x7D` = Backlight, `0x7E` = No backlight
- **LINE**     : Line. (`LINE & 0x80`) will request all lines. This limits the number of lines to `128`.
                 `LINE > 0x80` means to request a item on index (`LINE & 0x7F`) and continue with
                 (`LINE + 1`).
- **LEN**      : Length
- **MESSAGE**  : Message


### [0x13] MCP23017 message (MCP23017)

    |==========================================|
    | (A) Request                              |
    |------------------------------------------|
    |  0 |  1 |  2 |                           |
    | CRC|KIND|ADDR|                           |
    |==========================================|
    | (B) Response                             |
    |------------------------------------------|
    |  0 |  1 |  2 |   3 |   4 |               |
    | CRC|KIND|ADDR|GPIOA|GPIOB|               |
    |==========================================|
    | (C) Write 1 port                         |
    |------------------------------------------|
    |  0 |  1 |  2 |  3 |  4 |                 |
    | CRC|KIND|ADDR|PORT|GPIO|                 |
    |==========================================|
    | (D) Write both ports                     |
    |------------------------------------------|
    |  0 |  1 |  2 |  3 |  4  |   5 |          |
    | CRC|KIND|ADDR|0x03|GPIOA|GPIOB|          |
    |==========================================|

- **CRC**  : Checksum of the packet
- **KIND** : Message kind
- **ADDR** : I2C address
- **PORT** : `0x01` - port A, `0x02` - port B, `0x03` - port A and port B
- **GPIO** : GPIO value
- **GPIOA**: GPIO A value
- **GPIOB**: GPIO B value


### [0x14] PIR message (PIR)

    |==========================================|
    | (A) Request                              |
    |------------------------------------------|
    |  0 |  1 |                                |
    | CRC|KIND|                                |
    |==========================================|
    | (B) Response                             |
    |------------------------------------------|
    |  0 |  1 |   2 |                          |
    | CRC|KIND|VALUE|                          |
    |==========================================|

- **CRC**  : Checksum of the packet
- **KIND** : Message kind
- **VALUE**: Value


### [0x15] RGB LED Strip (RGB)

    |=======================================================================|
    | (A) Request concrete item of a concrete strip                         |
    |-----------------------------------------------------------------------|
    |  0 |  1 |  2 |  3 |                                                   |
    | CRC|KIND| NUM| IDX|                                                   |
    |=======================================================================|
    | (B) Configuration for default strip                                   |
    |-----------------------------------------------------------------------|
    |  0 |  1 |   2   |  3 |  4 |  5 |6...7|  8 |  9 |   10  |              |
    | CRC|KIND|PATTERN|  R |  G |  B |DELAY| MIN| MAX|TIMEOUT|              |
    |=======================================================================|
    | (C) Configuration for concrete strip NUM                              |
    |-----------------------------------------------------------------------|
    |  0 |  1 |  2 |   3   |  4 |  5 |  6 |7...8|  9 |  10|   11  |         |
    | CRC|KIND| NUM|PATTERN|  R |  G |  B |DELAY| MIN| MAX|TIMEOUT|         |
    |=======================================================================|
    | (D) Response                                                          |
    |-----------------------------------------------------------------------|
    |  0 |  1 |  2 |  3 |  4 |   5   |  6 |  7 |  8 |9..10| 11 | 12 |   13  |
    | CRC|KIND| NUM| CNT| IDX|PATTERN|  R |  G |  B |DELAY| MIN| MAX|TIMEOUT|
    |=======================================================================|

- **CRC**    : Checksum of the packet
- **KIND**   : Message kind
- **NUM**    : Strip number
- **IDX**    : Index of item to request. (`IDX & 0x80`) will request all items. This limits the number of items
               in a list to `128`. `IDX > 0x80` means to request a item on index (`IDX & 0x7F`) and continue with
               (`IDX + 1`).
- **PATTERN**: The requested pattern. (`PATTERN & 0x80` will replace the list with this one item)
- **R,G,B**  : Reg, green and blue component of the requested color
- **DELAY**  : Animation delay (depends on pattern implementation)
- **MIN,MAX**: Minimum and maximum color values (depends on pattern implementation)
- **TIMEOUT**: Number of times to repeat pattern animation before switching to next item in the list


### [0x16] WS281x RGB light strip (WS281x)

    |=================================================================================|
    | (A) Request concrete item of a concrete strip                                   |
    |---------------------------------------------------------------------------------|
    |  0 |  1 |  2 |  3 |                                                             |
    | CRC|KIND| NUM| IDX|                                                             |
    |=================================================================================|
    | (B) Configuration for default strip                                             |
    |---------------------------------------------------------------------------------|
    |  0 |  1 |   2   | 3  ... 23 |24 25|  26 |  27  | 28| 29|   30  |                |
    | CRC|KIND|PATTERN|RGB0...RGB7|DELAY|WIDTH|FADING|MIN|MAX|TIMEOUT|                |
    |=================================================================================|
    | (C) Configuration for concrete strip NUM                                        |
    |---------------------------------------------------------------------------------|
    |  0 |  1 |  2 |   3   | 4  ... 24 |25 26|  27 |  28  | 29| 30|   31  |           |
    | CRC|KIND| NUM|PATTERN|RGB0...RGB7|DELAY|WIDTH|FADING|MIN|MAX|TIMEOUT|           |
    |=================================================================================|
    | (D) Response                                                                    |
    |  0 |  1 |  2 |  3 |  4 |   5   |   6...26  |27 28|  29 |  30  | 31 | 32 |   33  |
    | CRC|KIND| NUM| CNT| IDX|PATTERN|RGB0...RGB7|DELAY|WIDTH|FADING| MIN| MAX|TIMEOUT|
    |=================================================================================|

- **CRC**    : Checksum of the packet
- **KIND**   : Message kind
- **NUM**    : Strip number
- **IDX**    : Index of item to request. (`IDX & 0x80`) will request all items. This limits the number of items
               in a list to `128`. `IDX > 0x80` means to request a item on index (`IDX & 0x7F`) and continue with
               (`IDX + 1`)
- **PATTERN**: The requested pattern. (`PATTERN & 0x80` will replace the list with this one item)
- **RGB0-7** : 8 colors, each consists of 3 bytes in the R, G, B order
- **DELAY**  : Animation delay (depends on pattern implementation)
- **WIDTH**  : Animation width (depends on pattern implementation)
- **FADING** : Animation fading  (depends on pattern implementation)
- **MIN,MAX**: Minimum and maximum color values (depends on pattern implementation)
- **TIMEOUT**: Number of times to repeat pattern animation before switching to next item in the list


### [0x17] WS281x RGB independent lights (WS281x_LIGHT)


### [0x20] Bluetooth settings (BT_SETTINGS)


### [0x21] Bluetooth EEPROM (BT_EEPROM)


### [0x80] State machine configuration (SM_CONFIGURATION)


### [0x81] State machine pull (SM_PULL)


### [0x82] State machine push (SM_PUSH)


### [0x83] State machine get (SM_GET_STATE)


### [0x84] State machine set state (SM_SET_STATE)


### [0x85] State machine action (SM_ACTION)


### [0x86] State machine input (SM_INPUT)


### [0xFE] Debug message (DEBUG)


### [0xFF] Unknown message (UNKNOWN)
