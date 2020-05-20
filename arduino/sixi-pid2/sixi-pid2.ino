//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t debugFlags;


void setup() {
  parser.setup();
  sensorManager.setup();
  motorManager.setup();
  
  // eepromLoadAll() calls eepromLoadHome() which needs to have motors already setup.
  eepromLoadAll();

  // make sure the starting target is the starting position (no move)
  parser.D18();

  //reportAllMotors();
  //clockISRProfile();

  clockSetup();

  parser.ready();
}


void reportAllMotors() {
  for(ALL_MOTORS(i)) {
    motors[i].report();
    Serial.print("\tsensor=");
    Serial.println(sensors[i].angle);
  }
  Serial.println();
}


void testPID() {
  int i=0;
  Serial.print(motors[i].stepsTarget);
  Serial.print('\t');
  Serial.println(motors[i].stepsNow);
}


void loop() {
  parser.update();
  sensorManager.update();
}
