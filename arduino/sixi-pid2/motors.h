#pragma once

#ifndef NUM_MOTORS
#error "NUM_MOTORS undefined"
#endif

#ifndef NUM_AXIES
#error "NUM_AXIES undefined"
#endif

// Motor and gearbox ratios

#define NORMAL_MOTOR_STEPS   200  // 1.8 degrees per step

#define MOTOR_STEPS_PER_TURN          (200.0)  // motor full steps * microstepping setting

#define NEMA17_CYCLOID_GEARBOX_RATIO        (20.0)
#define NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW  (35.0)
#define NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR (30.0)  // will be 40 if build using 3mm dowel pins
#define NEMA24_CYCLOID_GEARBOX_RATIO        (40.0)

#define DM322T_MICROSTEP              (2.0)

#define ELBOW_DOWNGEAR_RATIO          (30.0/20.0)
#define NEMA17_RATIO                  (DM322T_MICROSTEP*NEMA17_CYCLOID_GEARBOX_RATIO*ELBOW_DOWNGEAR_RATIO)
#define NEMA23_RATIO_ELBOW            (NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW)
#define NEMA23_RATIO_ANCHOR           (NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR)
#define NEMA24_RATIO                  (NEMA24_CYCLOID_GEARBOX_RATIO)

// Motors are numbered 0 (base) to 5 (hand)
#define MOTOR_0_STEPS_PER_TURN    (MOTOR_STEPS_PER_TURN*NEMA23_RATIO_ANCHOR)  // anchor
#define MOTOR_1_STEPS_PER_TURN    (MOTOR_STEPS_PER_TURN*NEMA24_RATIO)  // shoulder
#define MOTOR_2_STEPS_PER_TURN    (MOTOR_STEPS_PER_TURN*NEMA23_RATIO_ELBOW)  // elbow
#define MOTOR_3_STEPS_PER_TURN    (MOTOR_STEPS_PER_TURN*NEMA17_RATIO)  // ulna
#define MOTOR_4_STEPS_PER_TURN    (MOTOR_STEPS_PER_TURN*NEMA17_RATIO)  // wrist
#define MOTOR_5_STEPS_PER_TURN    (MOTOR_STEPS_PER_TURN*NEMA17_RATIO)  // hand

#define DEGREES_PER_STEP_0 (360.0/MOTOR_0_STEPS_PER_TURN)
#define DEGREES_PER_STEP_1 (360.0/MOTOR_1_STEPS_PER_TURN)
#define DEGREES_PER_STEP_2 (360.0/MOTOR_2_STEPS_PER_TURN)
#define DEGREES_PER_STEP_3 (360.0/MOTOR_3_STEPS_PER_TURN)
#define DEGREES_PER_STEP_4 (360.0/MOTOR_4_STEPS_PER_TURN)
#define DEGREES_PER_STEP_5 (360.0/MOTOR_5_STEPS_PER_TURN)

#define STEP_PER_DEGREES_0 (MOTOR_0_STEPS_PER_TURN/360.0)
#define STEP_PER_DEGREES_1 (MOTOR_1_STEPS_PER_TURN/360.0)
#define STEP_PER_DEGREES_2 (MOTOR_2_STEPS_PER_TURN/360.0)
#define STEP_PER_DEGREES_3 (MOTOR_3_STEPS_PER_TURN/360.0)
#define STEP_PER_DEGREES_4 (MOTOR_4_STEPS_PER_TURN/360.0)
#define STEP_PER_DEGREES_5 (MOTOR_5_STEPS_PER_TURN/360.0)

// step signal start
#define START0 LOW
#define START1 LOW
#define START2 LOW
#define START3 HIGH
#define START4 HIGH
#define START5 HIGH

// step signal end
#define END0 HIGH
#define END1 HIGH
#define END2 HIGH
#define END3 LOW
#define END4 LOW
#define END5 LOW


#include "MServo.h"


class StepperMotor {
public:
  char letter;

  uint8_t step_pin;
  uint8_t dir_pin;
  uint8_t enable_pin;

  // only a whole number of steps is possible.
  int32_t stepsUpdated;
  int32_t stepsNow;
  int32_t stepsTarget;
  
  float angleTarget;
  
  float limitMax;
  float limitMin;
  
  // current error
  // PID values
  float kp=0.0;
  float ki=0.0;
  float kd=0.0;
  
  float error;
  float error_i;
  float error_last;
  
  uint32_t timeSinceLastStep_us;
  uint32_t stepInterval_us;
  float velocity;

  
  StepperMotor() {
    stepsNow=0;
    stepsTarget=0;
    angleTarget=0;

    error=0;
    error_i=0;
    error_last=0;
    
    timeSinceLastStep_us=0;
    stepInterval_us=0;
  }

  /**
   * Called byt the ISR to adjust the position of each stepper motor.
   * MUST NOT contain Serial.* commands
   * @input dt microseconds since last update
   * @input angleNow degrees
   */
  void update(float dt,float angleNow);
  
  void setPID(float p,float i,float d) {
    kp=p;
    ki=i;
    kd=d;
  }

  void report();
};


class MotorManager {
public:
  void setup();
  void enableAll();
  void disableAll();
};


extern StepperMotor motors[NUM_AXIES];
extern MotorManager motorManager;

#if NUM_SERVOS>0
extern Servo servos[NUM_SERVOS];
#endif
