//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint32_t reportDelay = 0;  // how long since last D17 sent out

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

  if(TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_ERROR)) {
    if(TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR)) {
      Serial.println(F("\n\n** POSITION ERROR **\n"));
      CBI(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR);
    }
  } else {
    if(!TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR)) {
      SBI(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR);
    }
  }

  if(TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS)) {
    if (millis() > reportDelay) {
      reportDelay = millis() + 100;
      parser.D17();
    }
  }
}
