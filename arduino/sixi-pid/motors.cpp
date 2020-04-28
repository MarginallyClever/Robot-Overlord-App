//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


StepperMotor motors[NUM_MOTORS];

#if NUM_SERVOS>0
Servo servos[NUM_SERVOS];
#endif

int stepsCount = 0;


// dt = us
void StepperMotor::update(float dt,float angleNow) {
  // use a PID to control the motion.

  if(sensorReady) {
    //update stepsNow
    if( abs(stepsNow) - stepsCount != abs(stepsUpdated)) {
      stepsNow = stepsUpdated;
    } 
    stepsCount = 0;
    sensorReady = false;
  }
  
  // P term
  error = stepsTarget - stepsNow;
  //error = angleTarget - angleNow;
  
  // i term
  error_i += error * dt;          
  // d term
  float error_d = (error - error_last) / dt;
  // put it all together
  velocity = kp * ( error + ki * error_i + kd * error_d );

  error_last = error;

  if(abs(error) < 0.5) velocity = 0;

  if(abs(velocity) < 1e-4) {
    stepInterval_us = 0xFFFFFFFF;  // uint32_t max value
    timeSinceLastStep_us=0;
    return;
  } else {
    stepInterval_us = floor(1000.0 / abs(velocity));
  }

  timeSinceLastStep_us += dt;
  
  //CANT PRINT INSIDE ISR 
  // print("("+error+","+velocity+")\t");
  //stepsNow += velocity*dt;
  if( timeSinceLastStep_us >= stepInterval_us ) {
    stepsNow += velocity<0 ? -1 : 1;
    stepsCount++;
    if(!IS_DRYRUN) {
      digitalWrite( dir_pin, velocity<0 ? HIGH : LOW );
      digitalWrite( step_pin, HIGH );
      digitalWrite( step_pin, LOW  );
    }
    timeSinceLastStep_us -=stepInterval_us;
  }
}
