// Sixi PID test
// dan@marginallyclever.com

//#define UNIT1  // uncomment for the very first Sixi only

#include <SPI.h>  // pkm fix for Arduino 1.5
#include <Arduino.h>  // for type definitions
#include <EEPROM.h>
#include <stdint.h>
#include "speed_lookuptable.h"


// wrong board type set
#ifndef __AVR_ATmega2560__
  #error "Oops!  Make sure you have 'Arduino Mega 2560' selected from the 'Tools -> Boards' menu."
#endif

// EEPROM

#define FIRMWARE_VERSION        10    // Increment when adding new variables
#define SIZEOF_FLOAT_BYTES      (4)
#define SIZEOF_LONG_BYTES       (4)
#define ADDR_VERSION            0                          // 0..255 (1 byte)
#define ADDR_UUID               (ADDR_VERSION+1)
#define EEPROM_UUID_LENGTH      (SIZEOF_LONG_BYTES)
#define ADDR_LIMITS             (ADDR_UUID+EEPROM_UUID_LENGTH)
#define EEPROM_LIMITS_LENGTH    (2*NUM_MOTORS*SIZEOF_FLOAT_BYTES)
#define ADDR_HOME               (ADDR_LIMITS+EEPROM_LIMITS_LENGTH)
#define EEPROM_LIMITS_HOME      (NUM_MOTORS*SIZEOF_FLOAT_BYTES)
#define ADDR_CALIBRATION_LEFT   (ADDR_HOME+EEPROM_LIMITS_HOME)
#define ADDR_CALIBRATION_RIGHT  (ADDR_CALIBRATION_LEFT+SIZEOF_FLOAT_BYTES)

// MOTOR PINS

#define MAX_MOTORS                 (6)

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

#define MAX_BOARD_SERVOS          (1)
#define SERVO0_PIN                (13)


// SENSORS

// sensor bits, flags, and masks
#define BOTTOM_14_MASK       (0x3FFF)
#define SENSOR_TOTAL_BITS    (16)
#define SENSOR_DATA_BITS     (15)
#define SENSOR_ANGLE_BITS    (14)
#define SENSOR_ANGLE_PER_BIT (360.0/(float)((uint32_t)1<<SENSOR_ANGLE_BITS))  // 0.00549316406

// SENSOR PINS

#define PIN_SENSOR_CSEL_0   8
#define  PIN_SENSOR_CLK_0   9
#define PIN_SENSOR_MOSI_0   10
#define PIN_SENSOR_MISO_0   11

#define PIN_SENSOR_CSEL_1   2
#define  PIN_SENSOR_CLK_1   3
#define PIN_SENSOR_MOSI_1   4
#define PIN_SENSOR_MISO_1   5

#define PIN_SENSOR_CSEL_2   17
#define  PIN_SENSOR_CLK_2   16
#define PIN_SENSOR_MOSI_2   15
#define PIN_SENSOR_MISO_2   14

#define PIN_SENSOR_CSEL_3   21
#define  PIN_SENSOR_CLK_3   20
#define PIN_SENSOR_MOSI_3   19
#define PIN_SENSOR_MISO_3   18

#ifdef UNIT1
#define PIN_SENSOR_CSEL_4   29
#define  PIN_SENSOR_CLK_4   27
#define PIN_SENSOR_MOSI_4   25
#define PIN_SENSOR_MISO_4   23

#define PIN_SENSOR_CSEL_5   22
#define  PIN_SENSOR_CLK_5   24
#define PIN_SENSOR_MOSI_5   26
#define PIN_SENSOR_MISO_5   28

#else  // UNIT1
#define PIN_SENSOR_CSEL_4   22
#define  PIN_SENSOR_CLK_4   24
#define PIN_SENSOR_MOSI_4   26
#define PIN_SENSOR_MISO_4   28

#define PIN_SENSOR_CSEL_5   29
#define  PIN_SENSOR_CLK_5   27
#define PIN_SENSOR_MOSI_5   25
#define PIN_SENSOR_MISO_5   23

#endif  // UNIT1


#define NORMAL_MOTOR_STEPS   200  // 1.8 degrees per step

#define MACHINE_STYLE_NAME           "SIXI"
#define MACHINE_HARDWARE_VERSION     6  // yellow sixi 2019

#define NUM_MOTORS           (6)
#define NUM_SERVOS           (0)
#define NUM_TOOLS            (1)
#define NUM_SENSORS          (6)

#define MOTHERBOARD BOARD_SIXI_MEGA  // sixi only supports one motherboard right now


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


// behaviour flags
#define POSITION_ERROR_FLAG_CONTINUOUS   (1<<0)  // report position (d17) continuously?
#define POSITION_ERROR_FLAG_ERROR        (1<<1)  // has error occurred?
#define POSITION_ERROR_FLAG_FIRSTERROR   (1<<2)  // report the error once per occurrence
#define POSITION_ERROR_FLAG_ESTOP        (1<<3)  // check for error at all?


// for assembly in isr inner loop
#define A(CODE) " " CODE "\n\t"


// CLOCK 

// for timer interrupt control
#undef F_CPU
#define F_CPU                   (16000000L)

#define MAX_COUNTER             (65536L)  // 16 bits

#define TIMER_RATE            ((F_CPU)/8)

// optimize code, please
#define FORCE_INLINE         __attribute__((always_inline)) inline

// 1.8deg stepper, 20-tooth GT2 pulley, 1/16 microstepping = 160 steps/mm, 400mm/s = 480000 steps/s
#define CLOCK_MAX_STEP_FREQUENCY (480000L)
#define CLOCK_MIN_STEP_FREQUENCY (F_CPU/500000U)

#define TIMEOUT_OK (1000)

// convenience
#define PENDING(NOW,SOON) ((uint32_t)(NOW-(SOON))<0)
#define ELAPSED(NOW,SOON) (!PENDING(NOW,SOON))

#ifndef MIN_SEGMENT_TIME_US
#define MIN_SEGMENT_TIME_US  (20000)
#endif

#ifndef MAX_OCR1A_VALUE
#define MAX_OCR1A_VALUE (0xFFFF)
#endif

#define CLOCK_ADJUST(x) {  OCR1A = (x);  }  // microseconds

// serial comms
#define BAUD 57600
#define MAX_BUF 127


unsigned char _sreg=0;
inline void CRITICAL_SECTION_START() {
  _sreg = SREG;  cli();
}
inline void CRITICAL_SECTION_END() {
  SREG = _sreg;
}



float capRotationDegrees(double arg0,double centerPoint) {
  while(arg0<centerPoint-180) arg0 += 360;
  while(arg0>centerPoint+180) arg0 -= 360;
  
  return arg0;
}


extern uint32_t current_feed_rate;


struct StepperMotor {
  char letter;

  uint8_t step_pin;
  uint8_t dir_pin;
  uint8_t enable_pin;
  
  // steps to degrees ratio (gearbox)
  float ratio;

  // only a whole number of steps is possible.
  int32_t stepsNow;
  // only a whole number of steps is possible.
  int32_t stepsTarget;
  float angleTarget;
  
  float angleHome;
  float limitMax;
  float limitMin;
  
  // current error
  // PID values
  float kp=5, ki=0.001, kd=0.00001;
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
  
  void update(float dt) {
    // PID calculation
    error = stepsTarget - stepsNow;
    error_i += error * dt;          
    float error_d = (error - error_last) / dt;
    velocity = kp * error + ki * error_i + kd * error_d;
    error_last = error;

    if(abs(velocity) < 1e-6) {
      stepInterval = 0xFFFFFFFF;  // uint32_t max value
      return;
    } else {
      stepInterval = 1000000 / floor(abs(velocity));
    }

    timeSinceLastStep += 1000000 / current_feed_rate;
    
    //CANT PRINT INSIDE ISR 
    // print("("+error+","+velocity+")\t");
    //stepsNow += velocity*dt;
    if( timeSinceLastStep >= stepInterval ) {
      stepsNow += velocity<0 ? -1 : 1;
      //digitalWrite( dir_pin, velocity<0 ? HIGH : LOW );
      //digitalWrite( step_pin, HIGH );
      //digitalWrite( step_pin, LOW  );
      timeSinceLastStep = 0;
    }
  }
  
  float getDegrees() {
    return capRotationDegrees( stepsNow*ratio, 0 );
  }
  
  void setPID(float p,float i,float d) {
    kp=p;
    ki=i;
    kd=d;
  }
};


// GLOBALS


StepperMotor motors[6];
int robot_uid=0;

char sensorPins[4*NUM_SENSORS];
float sensorAngles[NUM_SENSORS];
uint8_t positionErrorFlags;


// Serial comm reception
char serialBuffer[MAX_BUF + 1]; // Serial buffer
int sofar;                      // Serial buffer progress
uint32_t lastCmdTimeMs;         // prevent timeouts
int32_t lineNumber = 0;        // make sure commands arrive in order
uint8_t lastGcommand = -1;
uint32_t reportDelay = 0;  // how long since last D17 sent out


// timer stuff
uint32_t current_feed_rate = 1000;
uint8_t isr_step_multiplier = 1;
uint32_t min_segment_time_us=MIN_SEGMENT_TIME_US;




// EEPROM



// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
uint32_t EEPROM_readLong(int ee) {
  uint32_t value = 0;
  byte* p = (byte*)(void*)&value;
  for (uint16_t i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}

// 2020-01-31 Dan added check to not update EEPROM if value is unchanged.
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
// returns true if the value was changed.
bool EEPROM_writeLong(int ee, uint32_t value) {
  if(EEPROM_readLong(ee) == value) return false;
  
  byte* p = (byte*)(void*)&value;
  for (uint16_t i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);

  return true;
}


/**
 * 
 */
char loadVersion() {
  return EEPROM.read(ADDR_VERSION);
}


/**
 * 
 */
void saveUID() {
  Serial.println(F("Saving UID."));
  EEPROM_writeLong(ADDR_UUID,(uint32_t)robot_uid);
}


/**
 * 
 */
void saveLimits() {
  Serial.println(F("Saving limits."));
  int i,j=ADDR_LIMITS;
  for(i=0;i<NUM_MOTORS;++i) {
    EEPROM_writeLong(j,motors[i].limitMax*100);
    j+=4;
    EEPROM_writeLong(j,motors[i].limitMin*100);
    j+=4;
  }
}


/**
 * 
 */
void loadLimits() {
  int i,j=ADDR_LIMITS;
  for(i=0;i<NUM_MOTORS;++i) {
    motors[i].limitMax = (float)EEPROM_readLong(j)/100.0f;
    j+=4;
    motors[i].limitMin = (float)EEPROM_readLong(j)/100.0f;
    j+=4;
    //Serial.print("Axis ");
    //Serial.print(i);
    //Serial.print(" Min ");
    //Serial.print(axies[i].limitMin);
    //Serial.print(" Max ");
    //Serial.print(axies[i].limitMax);
    //Serial.println();
  }
}


/**
 * @param limits NUM_MOTORS*2 floats.  Each pair is one float for max limit and one for min limit.
 */
void adjustLimits(float *limits) {
  Serial.println(F("Adjusting limits."));
  int i,j=0;
  int changed=0;
  float v;
  for(i=0;i<NUM_MOTORS;++i) {
    // max test
    v = floor(limits[j]*100.0f)/100.0f;
    if(v != motors[i].limitMax) {
      motors[i].limitMax = v;
      changed=1;
    }
    j++;
    // min test
    v = floor(limits[j]*100.0f)/100.0f;
    if(v != motors[i].limitMin) {
      motors[i].limitMin = v;
      changed=1;
    }
    j++;
  }

  if( changed != 0 ) {
    saveLimits();
  }
}


/**
 * 
 */
void saveHome() {
  Serial.println(F("Saving home."));
  int i,j=ADDR_HOME;
  for(i=0;i<NUM_MOTORS;++i) {
    EEPROM_writeLong(j,(uint32_t)(motors[i].angleHome*100.0f));
    j+=4;
  }
}


/**
 * 
 */
void loadHome() {
  int i,j=ADDR_HOME;
  for(i=0;i<NUM_MOTORS;++i) {
    motors[i].angleHome = (float)EEPROM_readLong(j)/100.0f;
    j+=4;
  }
}


/**
 * 
 */
void loadConfig() {
  char versionNumber = loadVersion();
  if( versionNumber != FIRMWARE_VERSION ) {
    // If not the current FIRMWARE_VERSION or the FIRMWARE_VERSION is sullied (i.e. unknown data)
    // Update the version number
    EEPROM.write(ADDR_VERSION,FIRMWARE_VERSION);
  }
  
  // Retrieve stored configuration
  robot_uid=EEPROM_readLong(ADDR_UUID);
  loadLimits();
  loadHome();
}













// SIXI





/**
   Inverse Kinematics turns XY coordinates into step counts from each motor
   This code is a duplicate of https://github.com/MarginallyClever/Robot-Overlord-App/blob/master/src/main/java/com/marginallyclever/robotOverlord/sixiRobot/java inverseKinematics()
   @param angles the cartesian coordinate
   @param steps a measure of each belt to that plotter position
*/
void IK(const float *const angles, int32_t *steps) {
  // each of the xyz motors are differential to each other.
  // to move only one motor means applying the negative of that value to the other two motors

  // consider a two motor differential: 
  // if x moves, subtract x from y.
  // if y moves, subtract y from x.
  // so for three axis,
  // for any axis N subtract the other two axies from this axis.

  // Some of these are negative because the motor is wired to turn the opposite direction from the Robot Overlord simulation.
  // Robot Overlord has the final say, so these are flipped to match the simulation.
  // This is the only place motor direction should ever be inverted.
  float J0 = -angles[0];  // anchor  (G0 X*)
  float J1 =  angles[1];  // shoulder (G0 Y*)
  float J2 =  angles[2];  // elbow (G0 Z*)
  float J3 = -angles[3];  // ulna  (G0 U*)
  float J4 =  angles[4];  // wrist (G0 V*)
  float J5 = -angles[5];  // hand  (G0 W*)

  // adjust for the wrist differential
  J5 += (J4/NEMA17_CYCLOID_GEARBOX_RATIO)+(J3/NEMA17_CYCLOID_GEARBOX_RATIO);
  J4 += (J3/NEMA17_CYCLOID_GEARBOX_RATIO);
  
  steps[0] = J0 * STEP_PER_DEGREES_0;  // ANCHOR
  steps[1] = J1 * STEP_PER_DEGREES_1;  // SHOULDER
  steps[2] = J2 * STEP_PER_DEGREES_2;  // ELBOW
  steps[3] = J3 * STEP_PER_DEGREES_3;  // ULNA
  steps[4] = J4 * STEP_PER_DEGREES_4;  // WRIST
  steps[5] = J5 * STEP_PER_DEGREES_5;  // HAND
  
  steps[NUM_MOTORS] = angles[6];
#ifdef DEBUG_IK
  Serial.print("J=");  Serial.print(J0);
  Serial.print('\t');  Serial.print(J1);
  Serial.print('\t');  Serial.print(J2);
  Serial.print('\t');  Serial.print(J3);
  Serial.print('\t');  Serial.print(J4);
  Serial.print('\t');  Serial.print(J5);
  Serial.print('\n');
#endif
}


/**
   Forward Kinematics - turns step counts into XY coordinates.  
   This code is a duplicate of https://github.com/MarginallyClever/Robot-Overlord-App/blob/master/src/main/java/com/marginallyclever/robotOverlord/sixiRobot/java forwardKinematics()
   @param steps a measure of each belt to that plotter position
   @param angles the resulting cartesian coordinate
   @return 0 if no problem, 1 on failure.
*/
int FK(uint32_t *steps, float *angles) {
  // TODO fill me in!

  return 0;
}


// turn on power to the motors (make them immobile)
void motor_engage() {
  for( int i=0; i<NUM_MOTORS; ++i ) {
    digitalWrite( motors[i].enable_pin, LOW );
  }
/*
  #if MACHINE_STYLE == SIXI
    // DM320T drivers want high for enabled
    digitalWrite(motors[4].enable_pin,HIGH);
    digitalWrite(motors[5].enable_pin,HIGH);
  #endif
*/
}

void robot_findHome() {
  motor_engage();
  // sixi always knows where it is.
}


void setupPins() {
  int i=0;

  sensorPins[i++]=PIN_SENSOR_CSEL_0;
  sensorPins[i++]=PIN_SENSOR_CLK_0;
  sensorPins[i++]=PIN_SENSOR_MISO_0;
  sensorPins[i++]=PIN_SENSOR_MOSI_0;
  
  sensorPins[i++]=PIN_SENSOR_CSEL_1;
  sensorPins[i++]=PIN_SENSOR_CLK_1;
  sensorPins[i++]=PIN_SENSOR_MISO_1;
  sensorPins[i++]=PIN_SENSOR_MOSI_1;
  
  sensorPins[i++]=PIN_SENSOR_CSEL_2;
  sensorPins[i++]=PIN_SENSOR_CLK_2;
  sensorPins[i++]=PIN_SENSOR_MISO_2;
  sensorPins[i++]=PIN_SENSOR_MOSI_2;
  
  sensorPins[i++]=PIN_SENSOR_CSEL_3;
  sensorPins[i++]=PIN_SENSOR_CLK_3;
  sensorPins[i++]=PIN_SENSOR_MISO_3;
  sensorPins[i++]=PIN_SENSOR_MOSI_3;
  
  sensorPins[i++]=PIN_SENSOR_CSEL_4;
  sensorPins[i++]=PIN_SENSOR_CLK_4;
  sensorPins[i++]=PIN_SENSOR_MISO_4;
  sensorPins[i++]=PIN_SENSOR_MOSI_4;
  
  sensorPins[i++]=PIN_SENSOR_CSEL_5;
  sensorPins[i++]=PIN_SENSOR_CLK_5;
  sensorPins[i++]=PIN_SENSOR_MISO_5;
  sensorPins[i++]=PIN_SENSOR_MOSI_5;

  for(i=0;i<NUM_SENSORS;++i) {
    pinMode(sensorPins[(i*4)+0],OUTPUT);  // csel
    pinMode(sensorPins[(i*4)+1],OUTPUT);  // clk
    pinMode(sensorPins[(i*4)+2],INPUT);  // miso
    pinMode(sensorPins[(i*4)+3],OUTPUT);  // mosi

    digitalWrite(sensorPins[(i*4)+0],HIGH);  // csel
    digitalWrite(sensorPins[(i*4)+3],HIGH);  // mosi
  }

  motors[0].letter = 'X';
  motors[0].ratio = DEGREES_PER_STEP_0;
  motors[0].step_pin        = MOTOR_0_STEP_PIN;
  motors[0].dir_pin         = MOTOR_0_DIR_PIN;
  motors[0].enable_pin      = MOTOR_0_ENABLE_PIN;

  motors[1].letter = 'Y';
  motors[1].ratio = DEGREES_PER_STEP_1;
  motors[1].step_pin        = MOTOR_1_STEP_PIN;
  motors[1].dir_pin         = MOTOR_1_DIR_PIN;
  motors[1].enable_pin      = MOTOR_1_ENABLE_PIN;

  motors[2].letter = 'Z';
  motors[2].ratio = DEGREES_PER_STEP_2;
  motors[2].step_pin        = MOTOR_2_STEP_PIN;
  motors[2].dir_pin         = MOTOR_2_DIR_PIN;
  motors[2].enable_pin      = MOTOR_2_ENABLE_PIN;

  motors[3].letter = 'U';
  motors[3].ratio = DEGREES_PER_STEP_3;
  motors[3].step_pin        = MOTOR_3_STEP_PIN;
  motors[3].dir_pin         = MOTOR_3_DIR_PIN;
  motors[3].enable_pin      = MOTOR_3_ENABLE_PIN;

  motors[4].letter = 'V';
  motors[4].ratio = DEGREES_PER_STEP_4;
  motors[4].step_pin        = MOTOR_4_STEP_PIN;
  motors[4].dir_pin         = MOTOR_4_DIR_PIN;
  motors[4].enable_pin      = MOTOR_4_ENABLE_PIN;

  motors[5].letter = 'W';
  motors[5].ratio = DEGREES_PER_STEP_5;
  motors[5].step_pin        = MOTOR_5_STEP_PIN;
  motors[5].dir_pin         = MOTOR_5_DIR_PIN;
  motors[5].enable_pin      = MOTOR_5_ENABLE_PIN;

  for (int i = 0; i < NUM_MOTORS; ++i) {
    // set the motor pin & scale
    pinMode(motors[i].step_pin, OUTPUT);
    pinMode(motors[i].dir_pin, OUTPUT);
    pinMode(motors[i].enable_pin, OUTPUT);
  }
}

/**
 * @param index the sensor to read
 * @param result where to store the returned value.  may be changed even if method fails.
 * @return 0 on fail, 1 on success.
// @see https://ams.com/documents/20143/36005/AS5147_DS000307_2-00.pdf
 */
bool getSensorRawValue(int index, uint16_t &result) {
  result=0;
  uint8_t input,parity=0;

  index*=4;
  
  // Send the request for the angle value (command 0xFFFF)
  // at the same time as receiving an angle.

  // Collect the 16 bits of data from the sensor
  digitalWrite(sensorPins[index+0],LOW);  // csel
  
  for(int i=0;i<SENSOR_TOTAL_BITS;++i) {
    digitalWrite(sensorPins[index+1],HIGH);  // clk
    // this is here to give a little more time to the clock going high.
    // only needed if the arduino is *very* fast.  I'm feeling generous.
    result <<= 1;
    digitalWrite(sensorPins[index+1],LOW);  // clk
    
    input = digitalRead(sensorPins[index+2]);  // miso
#ifdef VERBOSE
    Serial.print(input,DEC);
#endif
    result |= input;
    parity ^= (i>0) & input;
  }

  digitalWrite(sensorPins[index+0],HIGH);  // csel
  
  // check the parity bit
  return ( parity != (result>>SENSOR_DATA_BITS) );
}


/**
 * @param rawValue 16 bit value from as4157 sensor, including parity and EF bit
 * @return degrees calculated from bottom 14 bits.
 */
float extractAngleFromRawValue(uint16_t rawValue) {
  return (float)(rawValue & BOTTOM_14_MASK) * 360.0 / (float)(1<<SENSOR_ANGLE_BITS);
}

void sensorUpdate() {
  uint16_t rawValue;
  float v;
  //uint32_t aa,bb,cc,dd,ee;
  for(int i=0;i<NUM_SENSORS;++i) {
    if(getSensorRawValue(i,rawValue)) continue;
    v = extractAngleFromRawValue(rawValue);
    // Some of these are negative because the sensor is reading the opposite rotation from the Robot Overlord simulation.
    // Robot Overlord has the final say, so these are flipped to match the simulation.
    // This is the only place motor direction should ever be inverted.
    if(i!=1 && i!=2) v=-v;
    v -= motors[i].angleHome;
    // CAUTION!  if motors[i].angleHome is some really big number (uint32_t -1?) these while loops
    // will be very slow.  It could happen if EEPROM has garbage data and loadConfig() pulls it in
    // when the robot boots.
    while(v<-180) v+=360;
    while(v> 180) v-=360;
    sensorAngles[i] = v;
  }
}


/**
   Look for character /code/ in the buffer and read the float that immediately follows it.
   @return the value found.  If nothing is found, /val/ is returned.
   @input code the character to look for.
   @input val the return value if /code/ is not found.
*/
float parseNumber(char code, float val) {
  char *ptr = serialBuffer; // start at the beginning of buffer
  char *finale = serialBuffer + sofar;
  for (ptr = serialBuffer; ptr < finale; ++ptr) { // walk to the end
    if (*ptr == ';') break;
    if (toupper(*ptr) == code) { // if you find code on your walk,
      return atof(ptr + 1); // convert the digits that follow into a float and return it
    }
  }
  return val;  // end reached, nothing found, return default val.
}


/**
   @return 1 if the character is found in the serial buffer, 0 if it is not found.
*/
char hasGCode(char code) {
  char *ptr = serialBuffer; // start at the beginning of buffer
  char *finale = serialBuffer + sofar;
  for (ptr = serialBuffer; ptr < finale; ++ptr) { // walk to the end
    if (*ptr == ';') break;
    if (toupper(*ptr) == code) { // if you find code on your walk,
      return 1;  // found
    }
  }
  return 0;  // not found
}


/**
   @return 1 if CRC ok or not present, 0 if CRC check fails.
*/
char checkLineNumberAndCRCisOK() {
  // is there a line number?
  int32_t cmd = parseNumber('N', -1);
  if (cmd != -1 && serialBuffer[0] == 'N') { // line number must appear first on the line
    if ( cmd != lineNumber ) {
      // wrong line number error
      Serial.print(F("BADLINENUM "));
      Serial.println(lineNumber);
      return 0;
    }

    // is there a checksum?
    int i;
    for (i = strlen(serialBuffer) - 1; i >= 0; --i) {
      if (serialBuffer[i] == '*') {
        break;
      }
    }

    if (i >= 0) {
      // yes.  is it valid?
      char checksum = 0;
      int c;
      for (c = 0; c < i; ++c) {
        checksum ^= serialBuffer[c];
      }
      c++; // skip *
      int against = strtod(serialBuffer + c, NULL);
      if ( checksum != against ) {
        Serial.print(F("BADCHECKSUM "));
        Serial.println(lineNumber);
        return 0;
      }
    } else {
      Serial.print(F("NOCHECKSUM "));
      Serial.println(lineNumber);
      return 0;
    }

    // remove checksum
    serialBuffer[i] = 0;

    lineNumber++;
  }

  return 1;  // ok!
}

/**
 * D22
 * reset home position to the current angle values.
 */
void sixiResetSensorOffsets() {
  int i;
  // cancel the current home offsets
  for (i = 0; i < NUM_SENSORS; ++i) {
    motors[i].angleHome = 0;
  }
  // read the sensor
  sensorUpdate();
  // apply the new offsets
  for (i = 0; i < NUM_SENSORS; ++i) {
    motors[i].angleHome = sensorAngles[i];
  }
}

/**
   M114
   Print the X,Y,Z, feedrate, acceleration, and home position
*/
void where() {
  int i;
  for (i = 0; i < NUM_MOTORS; ++i) {
    Serial.print(motors[i].letter);
    Serial.print(motors[i].getDegrees());
    Serial.print(' ');
  }

//Serial.print('F');  Serial.print(feed_rate);  Serial.print(' ');
//Serial.print(F('A'));  Serial.print(acceleration);
  Serial.println();
}

/**
 * D17 report the 6 axis sensor values from the Sixi robot arm.
 */
void reportAllAngleValues() {
  Serial.print(F("D17"));
  for (int i = 0; i < NUM_MOTORS; ++i) {
    Serial.print('\t');
    Serial.print(sensorAngles[i], 2);
  }
  /*
    if(current_segment==last_segment) {
    // report estimated position
    Serial.print(F("\t-\t"));

    working_seg = get_current_segment();
    for (uint8_t i = 0; i < NUM_SENSORS; ++i) {
      //float diff = working_seg->a[i].expectedPosition - sensorAngles[i];
      //Serial.print('\t');
      //Serial.print(abs(diff),3);
      Serial.print('\t');
      Serial.print(working_seg->a[i].expectedPosition,2);
    }
    }*/

  Serial.print('\t');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_CONTINUOUS)!=0)?'+':'-');
  Serial.print(((positionErrorFlags & POSITION_ERROR_FLAG_ERROR) != 0) ? '+' : '-');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_FIRSTERROR)!=0)?'+':'-');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_ESTOP)!=0)?'+':'-');
  Serial.println();
}

/**
 * D18 copy sensor values to motor step positions.
 */
void copySensorsToMotorPositions() {
  float a[NUM_MOTORS];
  int i, j;
  int numSamples = 10;

  for (j = 0; j < NUM_MOTORS; ++j) a[j] = 0;

  // assert(NUM_SENSORS <= NUM_MOTORS);

  for (i = 0; i < numSamples; ++i) {
    sensorUpdate();
    for (j = 0; j < NUM_SENSORS; ++j) {
      a[j] += sensorAngles[j];
    }
  }
  for (j = 0; j < NUM_SENSORS; ++j) {
    motors[i].stepsNow = a[j] / (float)numSamples;
  }
}

/**
   prepares the input buffer to receive a new message and tells the serial connected device it is ready for more.
*/
void parserReady() {
  sofar = 0; // clear input buffer
  Serial.print(F("\n> "));  // signal ready to receive input
  lastCmdTimeMs = millis();
}

/**
 * G0/G1 linear moves
 */
void parseLine() {
  float angles[NUM_MOTORS];
  int32_t steps[NUM_MOTORS];

  Serial.println();
  
  for(int i=0;i<NUM_MOTORS;++i) {
    float parsed = parseNumber( motors[i].letter, motors[i].angleTarget );
    angles[i] = (int32_t)floor(parsed);
  }
  
  IK(angles,steps);

  CRITICAL_SECTION_START();
  for(int i=0;i<NUM_MOTORS;++i) {
//*
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleTarget);
    Serial.print('\t');
    Serial.print(motors[i].stepsTarget);
    Serial.print('\t');
    Serial.print(angles[i]);
    Serial.print('\t');
    Serial.print(steps[i]);
    Serial.print('\t');
    Serial.print(motors[i].stepsNow);
    Serial.println();
//*/
    motors[i].stepsTarget = steps[i];
  }
  CRITICAL_SECTION_END();
}


/**
 * process commands in the serial receive buffer
 */
void processCommand() {
  if( serialBuffer[0] == '\0' || serialBuffer[0] == ';' ) return;  // blank lines
  if(!checkLineNumberAndCRCisOK()) return; // message garbled
  
  // remove any trailing semicolon.
  int last = strlen(serialBuffer)-1;
  if( serialBuffer[last] == ';') serialBuffer[last]=0;
  
  if( !strncmp(serialBuffer, "UID", 3) ) {
    robot_uid = atoi(strchr(serialBuffer, ' ') + 1);
    saveUID();
  }

  int8_t cmd;

  // M codes
  cmd = parseNumber('M', -1);
  switch (cmd) {
    case 114:  where();  break;
    default:   break;
  }
  if (cmd != -1) return; // M command processed, stop.
  
  // machine style-specific codes
  cmd = parseNumber('D', -1);
  switch (cmd) {
    case 10:  // get hardware version
      Serial.print(F("D10 V"));
      Serial.println(MACHINE_HARDWARE_VERSION);
      break;
    case 17:  reportAllAngleValues();  break;
    case 18:  copySensorsToMotorPositions();  break;
    case 19:  positionErrorFlags ^= POSITION_ERROR_FLAG_CONTINUOUS;  break; // toggle
    case 20:  positionErrorFlags &= 0xffff ^ (POSITION_ERROR_FLAG_ERROR | POSITION_ERROR_FLAG_FIRSTERROR);  break; // off
    case 21:  positionErrorFlags ^= POSITION_ERROR_FLAG_ESTOP;  break; // toggle ESTOP
    case 22:  sixiResetSensorOffsets();  break;
    default:  break;
  }
  if (cmd != -1) return; // D command processed, stop.

  // no M or D commands were found.  This is probably a G-command.
  // G codes
  cmd = parseNumber('G', lastGcommand);
  lastGcommand = -1;
  switch (cmd) {
    case  0:
    case  1:
      lastGcommand = cmd;
      parseLine();
      break;
    default: break;
  }
}


void listenToSerial() {
  // listen for serial commands
  while (Serial.available() > 0) {
    char c = Serial.read();
    Serial.print(c);
    if (sofar < MAX_BUF) serialBuffer[sofar++] = c;
    if (c == '\r' || c == '\n') {
      serialBuffer[sofar - 1] = 0;

      // echo confirmation
      //Serial.println(serialBuffer);

      // do something with the command
      processCommand();
      parserReady();
    }
  }
}


// intRes = intIn1 * intIn2 >> 16
// uses:
// r26 to store 0
// r27 to store the byte 1 of the 24 bit result
static FORCE_INLINE uint16_t MultiU16X8toH16(uint8_t charIn1, uint16_t intIn2) {
  register uint8_t tmp;
  register uint16_t intRes;
  __asm__ __volatile__ (
    A("clr %[tmp]")
    A("mul %[charIn1], %B[intIn2]")
    A("movw %A[intRes], r0")
    A("mul %[charIn1], %A[intIn2]")
    A("add %A[intRes], r1")
    A("adc %B[intRes], %[tmp]")
    A("lsr r0")
    A("adc %A[intRes], %[tmp]")
    A("adc %B[intRes], %[tmp]")
    A("clr r1")
      : [intRes] "=&r" (intRes),
        [tmp] "=&r" (tmp)
      : [charIn1] "d" (charIn1),
        [intIn2] "d" (intIn2)
      : "cc"
  );
  return intRes;
}


// intRes = longIn1 * longIn2 >> 24
// uses:
// A[tmp] to store 0
// B[tmp] to store bits 16-23 of the 48bit result. The top bit is used to round the two byte result.
// note that the lower two bytes and the upper byte of the 48bit result are not calculated.
// this can cause the result to be out by one as the lower bytes may cause carries into the upper ones.
// B A are bits 24-39 and are the returned value
// C B A is longIn1
// D C B A is longIn2
//
static FORCE_INLINE uint16_t MultiU24X32toH16(uint32_t longIn1, uint32_t longIn2) {
#ifdef ESP8266
  uint16_t intRes = longIn1 * longIn2 >> 24;
#else // ESP8266
  register uint8_t tmp1;
  register uint8_t tmp2;
  register uint16_t intRes;
  __asm__ __volatile__(
    A("clr %[tmp1]")
    A("mul %A[longIn1], %B[longIn2]")
    A("mov %[tmp2], r1")
    A("mul %B[longIn1], %C[longIn2]")
    A("movw %A[intRes], r0")
    A("mul %C[longIn1], %C[longIn2]")
    A("add %B[intRes], r0")
    A("mul %C[longIn1], %B[longIn2]")
    A("add %A[intRes], r0")
    A("adc %B[intRes], r1")
    A("mul %A[longIn1], %C[longIn2]")
    A("add %[tmp2], r0")
    A("adc %A[intRes], r1")
    A("adc %B[intRes], %[tmp1]")
    A("mul %B[longIn1], %B[longIn2]")
    A("add %[tmp2], r0")
    A("adc %A[intRes], r1")
    A("adc %B[intRes], %[tmp1]")
    A("mul %C[longIn1], %A[longIn2]")
    A("add %[tmp2], r0")
    A("adc %A[intRes], r1")
    A("adc %B[intRes], %[tmp1]")
    A("mul %B[longIn1], %A[longIn2]")
    A("add %[tmp2], r1")
    A("adc %A[intRes], %[tmp1]")
    A("adc %B[intRes], %[tmp1]")
    A("lsr %[tmp2]")
    A("adc %A[intRes], %[tmp1]")
    A("adc %B[intRes], %[tmp1]")
    A("mul %D[longIn2], %A[longIn1]")
    A("add %A[intRes], r0")
    A("adc %B[intRes], r1")
    A("mul %D[longIn2], %B[longIn1]")
    A("add %B[intRes], r0")
    A("clr r1")
    : [intRes] "=&r" (intRes),
    [tmp1] "=&r" (tmp1),
    [tmp2] "=&r" (tmp2)
    : [longIn1] "d" (longIn1),
    [longIn2] "d" (longIn2)
    : "cc"
  );
#endif // ESP8266
  return intRes;
}

/**
   Set the clock 2 timer frequency.
   @input desired_freq_hz the desired frequency
   Different clock sources can be selected for each timer independently.
   To calculate the timer frequency (for example 2Hz using timer1) you will need:
*/
FORCE_INLINE unsigned short calc_timer(uint32_t desired_freq_hz, uint8_t*loops) {
  uint32_t timer;
  uint8_t step_multiplier = 1;

  int idx=0;
  while( idx<6 && desired_freq_hz > 10000 ) {
    step_multiplier <<= 1;
    desired_freq_hz >>= 1;
    idx++;
  }
  *loops = step_multiplier;
  
  if( desired_freq_hz < CLOCK_MIN_STEP_FREQUENCY ) desired_freq_hz = CLOCK_MIN_STEP_FREQUENCY;
  desired_freq_hz -= CLOCK_MIN_STEP_FREQUENCY;
  if(desired_freq_hz >= 8 *256) {
    const uint8_t tmp_step_rate = (desired_freq_hz & 0x00FF);
    const uint16_t table_address = (uint16_t)&speed_lookuptable_fast[(uint8_t)(desired_freq_hz >> 8)][0],
                   gain = (uint16_t)pgm_read_word_near(table_address + 2);
    timer = MultiU16X8toH16(tmp_step_rate, gain);
    timer = (uint16_t)pgm_read_word_near(table_address) - timer;
  } else { // lower step rates
    uint16_t table_address = (uint16_t)&speed_lookuptable_slow[0][0];
    table_address += ((desired_freq_hz) >> 1) & 0xFFFC;
    timer = (uint16_t)pgm_read_word_near(table_address)
          - (((uint16_t)pgm_read_word_near(table_address + 2) * (uint8_t)(desired_freq_hz & 0x0007)) >> 3);
  }
  
  return timer;
}



ISR(TIMER1_COMPA_vect) {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  // (AVR enters the ISR with global interrupts disabled, so no need to do it here)
  for( int j=0; j<isr_step_multiplier;++j ) {
    for( int i=0; i<NUM_MOTORS; ++i ) {
      motors[i].update(0.001);
    }
  }
  
  digitalWrite(13,digitalRead(13)==HIGH?LOW:HIGH);

  // Turn the interrupts back on (reduces UART delay, apparently)
}



void setup() {
  Serial.begin(BAUD);
  Serial.println(F("** WAKING **"));
  loadConfig();
  setupPins();
  
  // setup servos
#if NUM_SERVOS>0
  servos[0].attach(SERVO0_PIN);
#endif

  pinMode(13,OUTPUT);  // LED

  // find the starting position of the arm
  copySensorsToMotorPositions();
  
  // make sure the starting target is the starting position (no move)
  for (int i = 0; i < NUM_MOTORS; ++i) {
    motors[i].stepsTarget = motors[i].stepsNow;
  }
  
  positionErrorFlags = POSITION_ERROR_FLAG_CONTINUOUS;// | POSITION_ERROR_FLAG_ESTOP;

  // disable global interrupts
  CRITICAL_SECTION_START();
    // set entire TCCR1A register to 0
    TCCR1A = 0;
    // set the overflow clock to 0
    TCNT1  = 0;
    // set compare match register to desired timer count
    OCR1A = 2000;  // 1ms
    // turn on CTC mode
    TCCR1B = (1 << WGM12);
    // Set 8x prescaler
    TCCR1B = (TCCR1B & ~(0x07 << CS10)) | (2 << CS10);
    // enable timer compare interrupt
    TIMSK1 |= (1 << OCIE1A);
    
    uint32_t interval = calc_timer(current_feed_rate, &isr_step_multiplier);
    // set the next isr to fire at the right time.
    CLOCK_ADJUST(interval);
  CRITICAL_SECTION_END();

  Serial.print("interval=");  Serial.println(interval);
  Serial.println(F("** READY **"));
  parserReady();
}


void loop() {
  listenToSerial();
  sensorUpdate();

  if ((positionErrorFlags & POSITION_ERROR_FLAG_ERROR) != 0) {
    if ((positionErrorFlags & POSITION_ERROR_FLAG_FIRSTERROR) != 0) {
      Serial.println(F("\n\n** POSITION ERROR **\n"));
      positionErrorFlags &= 0xffff ^ POSITION_ERROR_FLAG_FIRSTERROR; // turn off
    }
  } else {
    if ((positionErrorFlags & POSITION_ERROR_FLAG_FIRSTERROR) == 0) {
      positionErrorFlags |= POSITION_ERROR_FLAG_FIRSTERROR; // turn on
    }
  }

  if ((positionErrorFlags & POSITION_ERROR_FLAG_CONTINUOUS) != 0) {
    if (millis() > reportDelay) {
      reportDelay = millis() + 100;
      reportAllAngleValues();
      /*
      for( int i=0;i<NUM_MOTORS;++i ) {
        Serial.print(motors[i].letter);
        Serial.print(motors[i].getDegrees());
        Serial.print('\t');
      }
      Serial.println();
      //*/
    }
  }
}
