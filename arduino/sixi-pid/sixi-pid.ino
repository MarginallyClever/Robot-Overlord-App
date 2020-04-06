//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"
//#include <Servo.h>

#if NUM_SERVOS>0
extern Servo servos[NUM_SERVOS];
#endif

void setupPins() {
  int i=0;

#define SSP(label,number) sensorPins[i++]=PIN_SENSOR_##label##_##number;
#define SSP2(number) \
  SSP(CSEL,number) \
  SSP(CLK,number) \
  SSP(MOSI,number) \
  SSP(MISO,number)

  SSP2(0);
  SSP2(1);
  SSP2(2);
  SSP2(3);
  SSP2(4);
  SSP2(5);

  for(i=0;i<NUM_SENSORS;++i) {
    pinMode(sensorPins[(i*4)+0],OUTPUT);  // csel
    pinMode(sensorPins[(i*4)+1],OUTPUT);  // clk
    pinMode(sensorPins[(i*4)+2],INPUT);  // miso
    pinMode(sensorPins[(i*4)+3],OUTPUT);  // mosi

    digitalWrite(sensorPins[(i*4)+0],HIGH);  // csel
    digitalWrite(sensorPins[(i*4)+3],HIGH);  // mosi
  }

#define SMP(LL,NN) \
  motors[NN].letter          = LL; \
  motors[NN].ratio           = DEGREES_PER_STEP_##NN; \
  motors[NN].step_pin        = MOTOR_##NN##_STEP_PIN; \
  motors[NN].dir_pin         = MOTOR_##NN##_DIR_PIN; \
  motors[NN].enable_pin      = MOTOR_##NN##_ENABLE_PIN;

  SMP('X',0);
  SMP('Y',1);
  SMP('Z',2);
  SMP('U',3);
  SMP('V',4);
  SMP('A',5);

  for (int i = 0; i < NUM_MOTORS; ++i) {
    // set the motor pin & scale
    pinMode(motors[i].step_pin, OUTPUT);
    pinMode(motors[i].dir_pin, OUTPUT);
    pinMode(motors[i].enable_pin, OUTPUT);
    //digitalWrite(motors[i].enable_pin,HIGH);
  }
  
  // setup servos
#if NUM_SERVOS>0
  servos[0].attach(SERVO0_PIN);
#endif
}

void setup() {
  Serial.begin(BAUD);
  
  loadConfig();
  
  setupPins();

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
    OCR1A = 2000;  // set the next isr to fire at the right time.
    // turn on CTC mode
    TCCR1B = (1 << WGM12);
    // Set 8x prescaler
    TCCR1B = (TCCR1B & ~(0x07 << CS10)) | (2 << CS10);
    // enable timer compare interrupt
    TIMSK1 |= (1 << OCIE1A);
    
    uint32_t interval = calc_timer(current_feed_rate, &isr_step_multiplier);
    CLOCK_ADJUST(interval);
  
  // enable global interrupts
  CRITICAL_SECTION_END();

  parserReady();
}


void reportAllTargets() {
  for( int i=0;i<NUM_MOTORS;++i ) {
    Serial.print(motors[i].letter);
    Serial.print(motors[i].stepsTarget);
    Serial.print('\t');
    //Serial.print(motors[i].stepsNow);
    //Serial.print('\t');
    //Serial.print(motors[i].error);
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
  serialUpdate();
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
      //reportAllTargets();
      //testPID();
    }
  }
}
