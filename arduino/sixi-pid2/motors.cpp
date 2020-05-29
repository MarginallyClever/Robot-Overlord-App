//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"

MotorManager motorManager;

StepperMotor motors[NUM_AXIES];

#if NUM_SERVOS>0
Servo servos[NUM_SERVOS];
#endif

int stepsCount = 0;


void StepperMotor::report() {
  //*
  Serial.print(letter);
  //Serial.print("\tpid=");  Serial.print(kp);
  //Serial.print(", ");      Serial.print(ki);
  //Serial.print(", ");      Serial.print(kd);
  Serial.print("\tangleTarget=");      Serial.print(angleTarget);
  Serial.print("\tstepsTarget=");      Serial.print(stepsTarget);
  Serial.print("\tstepsNow=");         Serial.print(stepsNow);
  Serial.print("\terror=");            Serial.print(error);
  //Serial.print("\tv=");                Serial.print(velocityActual);
  Serial.print("\tv=");                Serial.print(target_velocity);
  Serial.println();
  //*/
}


void StepperMotor::updateStepCount() {
  //if( abs(stepsNow) - stepsCount != abs(stepsUpdated)) {
    for(ALL_MOTORS(i)) {
      stepsNow = stepsUpdated;
      stepsCount = 0;
    }
  //}
}


// dt = us
void StepperMotor::update(float dt_us,float angleNow) {
  // use a PID to control the motion.

  // P term
  error = stepsTarget - stepsNow;
  //error = angleTarget - angleNow;

  interpolationTime += dt_us/1000000.0;
  if( interpolationTime > totalTime ) interpolationTime = totalTime;
  float vInterpolated = velocity;
  if(totalTime>0) {
    vInterpolated = velocity + ( target_velocity - velocity ) * interpolationTime / totalTime;
  } else {
    vInterpolated = target_velocity;
  }
    
  // i term
  error_i += error * dt_us;
  // d term
  float error_d = (error - error_last) / dt_us;
  // put it all together
  float positionInfluence = kp * ( error + ki * error_i + kd * error_d );
  velocityActual = vInterpolated + positionInfluence;

  error_last = error;

  if(abs(error) < 0.5) velocity = 0;

  if(abs(velocityActual) < 1e-4) {
    stepInterval_us = 0xFFFFFFFF;  // uint32_t max value
    timeSinceLastStep_us=0;
    return;
  } else {
    stepInterval_us = floor(1000000.0 / abs(velocityActual));
  }

  timeSinceLastStep_us += dt_us;
  
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


void MotorManager::setup() {
#define SMP(LL,NN) { motors[NN].letter     = LL; \
                     motors[NN].step_pin   = MOTOR_##NN##_STEP_PIN; \
                     motors[NN].dir_pin    = MOTOR_##NN##_DIR_PIN; \
                     motors[NN].enable_pin = MOTOR_##NN##_ENABLE_PIN; }
  SMP('X',0)
  SMP('Y',1)
  SMP('Z',2)
  SMP('U',3)
  SMP('V',4)
  SMP('W',5)

  for(ALL_MOTORS(i)) {
    // set the motor pin & scale
    pinMode(motors[i].step_pin, OUTPUT);
    pinMode(motors[i].dir_pin, OUTPUT);
    pinMode(motors[i].enable_pin, OUTPUT);
    motors[i].totalTime=0;
  }

  // setup servos
#if NUM_SERVOS>0
  servos[0].attach(SERVO0_PIN);
#endif
}
