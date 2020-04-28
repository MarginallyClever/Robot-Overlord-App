//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint32_t reportDelay = 0;  // how long since last D17 sent out

uint8_t debugFlags;



void setupPins() {
  int i=0;

// SSP(CSEL,0) is equivalent to sensorPins[i++]=PIN_SENSOR_CSEL_0;
#define SSP(label,NN)    sensorPins[i++] = PIN_SENSOR_##label##_##NN;
#define SSP2(NN)         if(NUM_SENSORS>NN) {  SSP(CSEL,NN)  SSP(CLK,NN)  SSP(MISO,NN)  SSP(MOSI,NN)  }
  SSP2(0)
  SSP2(1)
  SSP2(2)
  SSP2(3)
  SSP2(4)
  SSP2(5)

  for(ALL_SENSORS(i)) {
    // MUST match the order in SSP2() above
    pinMode(sensorPins[(i*4)+0],OUTPUT);  // csel
    pinMode(sensorPins[(i*4)+1],OUTPUT);  // clk
    pinMode(sensorPins[(i*4)+2],INPUT);  // miso
    pinMode(sensorPins[(i*4)+3],OUTPUT);  // mosi

    digitalWrite(sensorPins[(i*4)+0],HIGH);  // csel
    digitalWrite(sensorPins[(i*4)+3],HIGH);  // mosi
  }

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


void setup() {
  Serial.begin(BAUD);

  eepromLoadAll();

  setupPins();

  // make sure the starting target is the starting position (no move)
  parser.D18();

  //reportAllMotors();

  positionErrorFlags = 0;
  SET_BIT_ON(positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS);// | POSITION_ERROR_FLAG_ESTOP;
  
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

  if(TEST(positionErrorFlags,POSITION_ERROR_FLAG_ERROR)) {
    if(TEST(positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR)) {
      Serial.println(F("\n\n** POSITION ERROR **\n"));
      CBI(positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR);
    }
  } else {
    if(!TEST(positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR)) {
      SBI(positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR);
    }
  }

  if(TEST(positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS)) {
    if (millis() > reportDelay) {
      reportDelay = millis() + 100;
      parser.D17();
    }
  }
}
