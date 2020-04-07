#pragma once


#define MAX_MOTORS                (6)
#define MAX_SERVOS                (1)

#define NUM_MOTORS                (6)
#define NUM_SERVOS                (0)

// use in for(ALL_MOTORS(i)) { //i will be rising
#define ALL_MOTORS(NN) int NN=0;NN<NUM_MOTORS;++NN

// MOTOR PINS

#define MOTOR_0_DIR_PIN           46
#define MOTOR_0_STEP_PIN          45
#define MOTOR_0_ENABLE_PIN        47

#define MOTOR_1_DIR_PIN           43
#define MOTOR_1_STEP_PIN          42
#define MOTOR_1_ENABLE_PIN        44

#define MOTOR_2_DIR_PIN           40
#define MOTOR_2_STEP_PIN          39
#define MOTOR_2_ENABLE_PIN        41

#define MOTOR_3_DIR_PIN           37
#define MOTOR_3_STEP_PIN          36
#define MOTOR_3_ENABLE_PIN        38

#define MOTOR_4_DIR_PIN           34
#define MOTOR_4_STEP_PIN          33
#define MOTOR_4_ENABLE_PIN        35

#define MOTOR_5_DIR_PIN           31
#define MOTOR_5_STEP_PIN          30
#define MOTOR_5_ENABLE_PIN        32

#define SERVO0_PIN                (13)

// Motor and gearbox ratios

#define NORMAL_MOTOR_STEPS   200  // 1.8 degrees per step

#define MOTOR_STEPS_PER_TURN          (200.0)  // motor full steps * microstepping setting

#define NEMA17_CYCLOID_GEARBOX_RATIO        (20.0)
#define NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW  (35.0)
#define NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR (30.0)
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


extern uint32_t current_feed_rate;


class StepperMotor {
public:
  char letter;

  uint8_t step_pin;
  uint8_t dir_pin;
  uint8_t enable_pin;

  // only a whole number of steps is possible.
  int32_t stepsNow;
  int32_t stepsTarget;
  
  float angleTarget;
  float angleHome;
  
  float limitMax;
  float limitMin;
  
  // current error
  // PID values
  float kp=5;
  float ki=0.01;
  float kd=0.00001;
  
  float error;
  float error_i;
  float error_last;
  
  uint32_t timeSinceLastStep;
  uint32_t stepInterval;
  float velocity;

  
  StepperMotor() {
    stepsNow=0;
    stepsTarget=0;
    angleTarget=0;
    angleHome=0;

    error=0;
    error_i=0;
    error_last=0;
    
    timeSinceLastStep=0;
    stepInterval=0;
  }

  /**
   * Called byt the ISR to adjust the position of each stepper motor.
   * MUST NOT contain Serial.* commands
   */
  void update(float dt,float angleNow);
  
  void setPID(float p,float i,float d) {
      kp=p;
      ki=i;
      kd=d;
  }

  void report() {
    Serial.println(letter);
    Serial.print("\tstepsTarget=");      Serial.println(stepsTarget);
    Serial.print("\tstepsNow=");         Serial.println(stepsNow);
    Serial.print("\terror=");            Serial.println(error);
    Serial.print("\tangleHome=");        Serial.println(angleHome);
    Serial.print("\tangleTarget=");      Serial.println(angleTarget);
  }
};


extern StepperMotor motors[NUM_MOTORS];
