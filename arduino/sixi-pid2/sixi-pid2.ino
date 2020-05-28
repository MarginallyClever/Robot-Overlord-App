//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t debugFlags=BIT_FOR_FLAG(FLAG_ECHO);
//uint8_t debugFlags=0;


void reportAllMotors() {
  int i=0;//for(ALL_MOTORS(i)) 
  {
    motors[i].report();/*
    Serial.print("\tsensorHome=");
    Serial.println(sensorManager.sensors[i].angleHome);
    Serial.print("\tsensor=");
    Serial.println(sensorManager.sensors[i].angle);//*/
  }
  //Serial.println();
}


void testPID() {
  int i=0;
  Serial.print(motors[i].stepsTarget);
  Serial.print('\t');
  Serial.println(motors[i].stepsNow);
}


void setup() {
  parser.setup();
  eeprom.loadAll();
  motorManager.setup();
  sensorManager.setup();
  
  // make sure the starting target is the starting position (no move)
  parser.D18();

  motors[0].setPID(0,0,0);
  motors[1].setPID(0,0,0);
  motors[2].setPID(0,0,0);
  motors[3].setPID(0,0,0);
  motors[4].setPID(0,0,0);
  motors[5].setPID(0,0,0);

  reportAllMotors();
  //clockISRProfile();

  clockSetup();

  parser.ready();
}


void loop() {
  parser.update();
  sensorManager.update();
  //reportAllMotors();
}
