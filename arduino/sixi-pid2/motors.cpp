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
  Serial.println(letter);
  Serial.print("\tpid=");  Serial.print(kp);
  Serial.print(", ");      Serial.print(ki);
  Serial.print(", ");      Serial.println(kd);
  /*
  Serial.print("\tstepsTarget=");      Serial.println(stepsTarget);
  Serial.print("\tstepsNow=");         Serial.println(stepsNow);
  Serial.print("\terror=");            Serial.println(error);
  Serial.print("\tangleTarget=");      Serial.println(angleTarget);
  //*/
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
  }

  // setup servos
#if NUM_SERVOS>0
  servos[0].attach(SERVO0_PIN);
#endif
}


// dt = us
void StepperMotor::update(float dt,float angleNow) {
  // use a PID to control the motion.

  if(sensorManager.sensorReady) {
    //update stepsNow
    if( abs(stepsNow) - stepsCount != abs(stepsUpdated)) {
      stepsNow = stepsUpdated;
    } 
    stepsCount = 0;
    sensorManager.sensorReady = false;
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
    stepInterval_us = floor(1000000.0 / abs(velocity));
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
