//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"
//#include <Servo.h>

StepperMotor motors[NUM_MOTORS];

#if NUM_SERVOS>0
Servo servos[NUM_SERVOS];
#endif



void StepperMotor::update(float dt) {
  // PID P term
  error = stepsTarget - stepsNow;
  // i term
  error_i += error * dt;          
  // d term
  float error_d = (error - error_last) / dt;
  // put it all together
  velocity = kp * ( error + ki * error_i + kd * error_d );

  error_last = error;

  if(abs(velocity) < 1e-4) {
    stepInterval = 0xFFFFFFFF;  // uint32_t max value
    return;
  } else {
    stepInterval = floor(1000000.0 / abs(velocity));
  }

  timeSinceLastStep += MIN_SEGMENT_TIME_US;
  
  //CANT PRINT INSIDE ISR 
  // print("("+error+","+velocity+")\t");
  //stepsNow += velocity*dt;
  if( timeSinceLastStep >= stepInterval ) {
    stepsNow += velocity<0 ? -1 : 1;
    digitalWrite( dir_pin, velocity<0 ? HIGH : LOW );
    digitalWrite( step_pin, HIGH );
    timeSinceLastStep =0;
    digitalWrite( step_pin, LOW  );
  }
}

