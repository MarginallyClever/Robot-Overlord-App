//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"



uint32_t reportDelay = 0;  // how long since last D17 sent out



void setupPins() {
  int i=0;

// SSP(CSEL,0) is equivalent to sensorPins[i++]=PIN_SENSOR_CSEL_0;
#define SSP(label,NN)    sensorPins[i++] = PIN_SENSOR_##label##_##NN;
#define SSP2(NN)         SSP(CSEL,NN) \
                         SSP(CLK,NN) \
                         SSP(MISO,NN) \
                         SSP(MOSI,NN)
                         
  SSP2(0);
  SSP2(1);
  SSP2(2);
  SSP2(3);
  SSP2(4);
  SSP2(5);

  for(ALL_SENSORS(i)) {
    // MUST match the order in SSP2() above
    pinMode(sensorPins[(i*4)+0],OUTPUT);  // csel
    pinMode(sensorPins[(i*4)+1],OUTPUT);  // clk
    pinMode(sensorPins[(i*4)+2],INPUT);  // miso
    pinMode(sensorPins[(i*4)+3],OUTPUT);  // mosi

    digitalWrite(sensorPins[(i*4)+0],HIGH);  // csel
    digitalWrite(sensorPins[(i*4)+3],HIGH);  // mosi
  }

#define SMP(LL,NN) \
  motors[NN].letter          = LL; \
  motors[NN].step_pin        = MOTOR_##NN##_STEP_PIN; \
  motors[NN].dir_pin         = MOTOR_##NN##_DIR_PIN; \
  motors[NN].enable_pin      = MOTOR_##NN##_ENABLE_PIN;

  SMP('X',0);
  SMP('Y',1);
  SMP('Z',2);
  SMP('U',3);
  SMP('V',4);
  SMP('W',5);

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


void setup() {
  Serial.begin(BAUD);
  
  eepromLoadAll();
  
  setupPins();

  // make sure the starting target is the starting position (no move)
  parser.D18();

  //reportAllMotors();
  
  positionErrorFlags = POSITION_ERROR_FLAG_CONTINUOUS;// | POSITION_ERROR_FLAG_ESTOP;
//*
  motors[0].setPID(1.0 , 0.03, 0.8);        //Good, But slow
  motors[1].setPID(1.25, 0.5, 0.25);        //Decent
  motors[2].setPID(1.25, 0.7, 0.25);        //Very Good - positive side has offset of 1-2 degrees
  motors[3].setPID(1.25, 0.85, 0.4);        //OFFSET IS HAPPENING 
  motors[4].setPID(1.25, 0.5, 0.25);        //OFFSET IS HAPPENING 
  motors[5].setPID(1.25, 0.5, 0.25);        //OFFSET IS HAPPENING 
/*/
  motors[0].setPID(500 , 1, 0.1);
  motors[1].setPID(500 , 1, 0.1);
  motors[2].setPID(500 , 1, 0.1);
  motors[3].setPID(500 , 1, 0.1);
  motors[4].setPID(500 , 1, 0.1);
  motors[5].setPID(500 , 1, 0.1);
//*/
  //clockISRProfile();
  
  clockSetup();

  parser.ready();
}


void reportAllMotors() {
  for(ALL_MOTORS(i)) {
    motors[i].report();
    Serial.print("\tsensor=");
    Serial.println(sensorAngles[i]);
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
  parser.update();
  sensorUpdate();
  sensorReady = true;

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
      parser.D17();
    }
  }
}
