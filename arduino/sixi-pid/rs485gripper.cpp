//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"

#ifdef HAS_GRIPPER_ROBOTIQ

RS485Gripper gripper;

SoftwareSerial gripperSerial(GRIPPER_RX, GRIPPER_TX);


void RS485Gripper::setup() {
  gripperSerial.begin(GRIPPER_BAUD);
}

void RS485Gripper::update() {
  if (gripperSerial.available()) {
    Serial.write(gripperSerial.read());
  }
}


#endif  // HAS_GRIPPER_ROBOTIQ
