#pragma once
// follows HAND-E_Manual_Generic_PDF_20200528.pdf
//
// The gripper can be controlled by Modbus RTU directly with RS485.
//
// For a general introduction to Modbus RTU and for details regarding the CRC algorithm, the reader is invited to read the Modbus over
// serial line specification and implementation guide available at: http://www.modbus.org/docs/Modbus_over_serial_line_V1_02.pdf.
//
// Modbus RTU is a communication protocol based on a Big Endian byte order.
// Modbus RTU specifications and details can be found at www.modbus.org.

#ifdef HAS_GRIPPER_ROBOTIQ

#define GRIPPER_RX        12
#define GRIPPER_TX        13
#define GRIPPER_BAUD      115200
#define GRIPPER_SLAVE_ID  9

// REGISTERS
// 16 one-byte registers
#define GRIPPER_FIRST_INPUT_REGISTER   1000
#define GRIPPER_FIRST_OUTPUT_REGISTER  2000

#define GRIPPER_MAX_INPUT_REGISTERS    6  // based on number of bytes described below
#define GRIPPER_MAX_OUTPUT_REGISTERS   6  // based on number of bytes described below

// Send registers
#define GRIPPER_REG_ACTION_REQUEST     0
  // action request bytes.  high to activate.
  #define GRIPPER_BIT_rACT             0  // enable pin?  Reset to clear fault status.
  #define GRIPPER_BIT_rGTO             3  // go to desired position.
  #define GRIPPER_BIT_rATR             4  // automatic release - slow opening of all grippers to maximum.  Emergency auto-release.
  #define GRIPPER_BIT_rARD             5  // auto release direction.  high for open, low for close.

#define GRIPPER_REG_POSITION_REQUEST   2  // 0...255.  0 is fully open, 255 is fully closed. 0.2mm per bit, 50mm range.  set this before setting rGTO.
#define GRIPPER_REG_SPEED              4  // 0...255.  Gripper speed.  255 is max.  set this before setting rGTO. 
#define GRIPPER_REG_FORCE              5  // 0...255.  Gripper force.  255 is max.  set this before setting rGTO.


// Recv registers
#define GRIPPER_REG_STATUS             0
  // activation status.
  #define GRIPPER_BIT_gACT             0
  // goto status.  0 for stopped (or performing activation / automatic release).
  #define GRIPPER_BIT_gGTO             3
  // 0x00 - Gripper is in reset (or automatic release) state. See Fault Status if gripper is activated.
  // 0x01 - Activation in progress.
  // 0x02 - Not used.
  // 0x03 - Activation is completed.
  #define GRIPPER_BIT_gSTA1            4 
  #define GRIPPER_BIT_gSTA2            5
  // 0x00 - Fingers are in motion towards requested position. No object detected.
  // 0x01 - Fingers have stopped due to a contact while opening before requested position. Object detected opening.
  // 0x02 - Fingers have stopped due to a contact while closing before requested position. Object detected closing.
  // 0x03 - Fingers are at requested position. No object detected or object has been loss / dropped.
  #define GRIPPER_BIT_gOBJ1            6
  #define GRIPPER_BIT_gOBJ2            7
  
#define GRIPPER_REG_FAULT_STATUS       2
  // 0x00 - No fault (solid blue LED)
  // # Priority faults (solid blue LED)
  // 0x05 - Action delayed; the activation (re-activation) must be completed prior to perform the action.
  // 0x07 - The activation bit must be set prior to performing the action.
  // # Minor faults (solid red LED)
  // x08 - Maximum operating temperature exceeded (≥ 85 °C internally); let cool down (below 80 °C).
  // 0x09 - No communication during at least 1 second.
  // # Major faults (LED blinking red/blue) - Reset is required (rising edge on activation bit (rACT) needed).
  // 0x0A - Under minimum operating voltage.
  // 0x0B - Automatic release in progress.
  // 0x0C - Internal fault, contact support@robotiq.com
  // 0x0D - Activation fault, verify that no interference or other error occurred.
  // 0x0E - Overcurrent triggered.
  // 0x0F - Automatic release completed.
  #define GRIPPER_BIT_gFLT0            0
  #define GRIPPER_BIT_gFLT1            1
  #define GRIPPER_BIT_gFLT2            2
  #define GRIPPER_BIT_gFLT3            3
  // per-gripper values.
  #define GRIPPER_BIT_kFLT0            4
  #define GRIPPER_BIT_kFLT1            5
  #define GRIPPER_BIT_kFLT2            6
  #define GRIPPER_BIT_kFLT3            7
  
#define GRIPPER_REG_POS_REQUEST_ECHO   3  // 0...255.  Last requested gripper position.  255 is fully closed.
#define GRIPPER_REG_POSITION           4  // 0...255.  Gripper position.  255 is fully closed.
#define GRIPPER_REG_CURRENT            5  // 0...255.  Current from motor drive, where current = (10*current) mA.


// must be inverted later!
#define GRIPPER_POS_MAX  255
#define GRIPPER_POS_MIN  0

#define GRIPPER_VEL_MAX  255
#define GRIPPER_VEL_MIN  0

#define GRIPPER_F_MAX    255
#define GRIPPER_F_MIN    0

// function messages
// read values: slave id, 4, address of first read register, number of read registers, CRC
// response:    slave id, 4, address of first read register, number of read registers, [bytes of data], CRC
#define GRIPPER_FUNCTION_READ_REGISTER  4
// set values:  slave id, 16, address of first write register, number of write registers, [bytes of data], CRC
// response:    slave id, 16, address of first write register, number of write registers, CRC
#define GRIPPER_FUNCTION_SET_REGISTER   16
// set values:  slave id, 23, address of first read register, number of read registers, [bytes of data], address of first write register, number of write registers, [bytes of data], CRC
// response:    slave id, 23, address of first read register, number of read registers, [bytes of data], CRC
#define GRIPPER_FUNCTION_BOTH_REGISTER  23


#include <SoftwareSerial.h>


class RS485Gripper {
public:  
  void setup();
  void update();
};

extern RS485Gripper gripper;

#endif // HAS_GRIPPER_ROBOTIQ
