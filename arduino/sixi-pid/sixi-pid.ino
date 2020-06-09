//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


//uint8_t debugFlags=0;
uint8_t debugFlags=BIT_FOR_FLAG(FLAG_ECHO);
uint32_t reportDelay;  // how long since last D17 sent out


void reportAllMotors() {
  int i=0;
  //for(ALL_MOTORS(i)) 
  {
    /*
    motors[i].report();
    Serial.print("\tsensorHome=");
    Serial.println(sensorManager.sensors[i].angleHome);
    Serial.print("\tsensor=");
    Serial.println(sensorManager.sensors[i].angle);
    //*/
    //Serial.print(usPerTickISR);
    //Serial.print('\t');
    //Serial.print(motors[i].letter);
    
    Serial.print(motors[i].kp);    Serial.print(' ');
    Serial.print(motors[i].ki);    Serial.print(' ');
    //Serial.print(motors[i].kd);    //Serial.print(' ');
    Serial.print(motors[i].error);    Serial.print(' ');
    Serial.print(motors[i].error_i);    Serial.print(' ');
    Serial.print(motors[i].stepsTarget);  Serial.print(' ');
    Serial.print(motors[i].stepsNow[motors[i].currentPlannerStep]);  Serial.print(' ');
    Serial.print(motors[i].stepsNow[NEXT_PLANNER_STEP(motors[i].currentPlannerStep)]);

    //Serial.print(motors[i].angleTarget);
    //Serial.print('/');
    //Serial.print(sensorManager.sensors[i].angle);
    
    //Serial.print('\t');
    //Serial.print(motors[i].velocityActual,2);
    //Serial.print('\t');
    //Serial.print(motors[i].stepInterval_us);
    Serial.print('\t');
  }
  Serial.println();
}

void testPID() {
  int i=0;
  Serial.print(motors[i].stepsTarget);
  Serial.print('\t');
  Serial.println(motors[i].stepsNow[motors[i].currentPlannerStep]);
}

void meanwhile() {
  // stop moving if I haven't received instruction in a while.
  if( millis() - parser.lastCmdTimeMs > G0_SAFETY_TIMEOUT_MS*2 ) {
    // and since I'm confused, set all my targets to 0.
    for( ALL_MOTORS(i) ) {
      motors[i].velocityTarget = 0;
      motors[i].error = 0;
      motors[i].error_i = 0;
    }
  }

  // update the velocities
  float sensorAngles[NUM_SENSORS];
  int32_t steps[NUM_MOTORS];
  for(ALL_SENSORS(i)) {
    sensorAngles[i] = sensorManager.sensors[i].angle;
  }
  kinematics.anglesToSteps(sensorAngles, steps);

  for( ALL_MOTORS(i) ) {
    motors[i].updatePID(steps[i]);
  }

  // report what I'm doing.
  if( REPORT_ANGLES_CONTINUOUSLY ) {
    if( millis() > reportDelay ) {
      reportDelay = millis() + 100;
      parser.D17();
      //reportAllMotors();
    }
  }

  // The PC will wait forever for the ready signal.
  // if Arduino hasn't received a new instruction in a while, send ready() again
  // just in case USB garbled ready and each half is waiting on the other.
  if( millis() - parser.lastCmdTimeMs > TIMEOUT_OK ) {
#ifdef HAS_TMC2130
    {
      uint32_t drv_status = driver_0.DRV_STATUS();
      uint32_t stallValue = (drv_status & SG_RESULT_bm) >> SG_RESULT_bp;
      Serial.print(stallValue, DEC);
      Serial.print('\t');
    }
    {
      uint32_t drv_status = driver_1.DRV_STATUS();
      uint32_t stallValue = (drv_status & SG_RESULT_bm) >> SG_RESULT_bp;
      Serial.println(stallValue, DEC);
    }
#endif
    parser.ready();
  }
}

void setup() {
  parser.setup();
  eeprom.loadAll();
  motorManager.setup();
  sensorManager.setup();
  
  // make sure the starting target is the starting position (no move)
  parser.D18();

  motors[0].setPID(10.0,5.0,0.0);
  motors[1].setPID(10.0,5.0,0.0);
  motors[2].setPID(10.0,5.0,0.0);
  motors[3].setPID(10.0,5.0,0.0);
  motors[4].setPID(10.0,5.0,0.0);
  motors[5].setPID(10.0,5.0,0.0);
  
#define REPORT_SPD(NN) Serial.println(MOTOR_##NN##_STEPS_PER_TURN/2);
#define MACRO6(AA)  AA(0) AA(1) AA(2) AA(3) AA(4) AA(5)
  //MACRO6(REPORT_SPD);

  //reportAllMotors();
  clockISRProfile();

  reportDelay=0;
  
  clockSetup();

  parser.ready();
}


void loop() {
  parser.update();
  sensorManager.update();
  meanwhile();
}
