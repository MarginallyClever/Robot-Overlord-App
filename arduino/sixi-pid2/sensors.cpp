//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


SensorManager sensorManager;


void SensorAS5147::start() {
  pinMode(pin_CSEL,OUTPUT);
  pinMode(pin_CLK ,OUTPUT);
  pinMode(pin_MISO,INPUT);
  pinMode(pin_MOSI,OUTPUT);
  
  digitalWrite(pin_CSEL,HIGH);
}


/**
 * @param result where to store the returned value.  may be changed even if method fails.
 * @return 0 on fail, 1 on success.
// @see https://ams.com/documents/20143/36005/AS5147_DS000307_2-00.pdf
 */
boolean SensorAS5147::getRawValue(uint16_t &result) {
  uint8_t input, parity=0;
  
  //uint16_t command=0xFFFE;  // Please return measured angle without dynamic angle error compensation
  uint16_t command=0xFFFF;  // Please return measured angle with    dynamic angle error compensation
  
  result=0;
  // Send the request for the angle value (command 0xFFFF) at the same time as receiving an angle.
  // This is done by leaving MOSI high all the time.

  // Collect the 16 bits of data from the sensor
  digitalWrite(pin_CSEL,LOW);
  
  for(int i=0;i<SENSOR_TOTAL_BITS;++i) {
    digitalWrite(pin_MOSI,(command>>15)==1?HIGH:LOW);
    //Serial.print((command>>15)?"1":"0");
    command<<=1;
    
    digitalWrite(pin_CLK,HIGH);  // clk
    // this is here to give a little more time to the clock going high.
    // only needed if the arduino is *very* fast.  I'm feeling generous.
    result <<= 1;
    digitalWrite(pin_CLK,LOW);  // clk
    
    input = digitalRead(pin_MISO);  // miso
#ifdef VERBOSE
    Serial.print(input,DEC);
#endif
    result |= input;
    parity ^= (i>0) & input;
  }
  //Serial.println();

  digitalWrite(pin_CSEL,HIGH);  // csel
  
  // check the parity bit
  return ( parity != (result>>SENSOR_DATA_BITS) );
}



void SensorManager::setup() {
  int i=0;

// SSP(CSEL,0) is equivalent to sensorPins[i++]=PIN_SENSOR_CSEL_0;
#define SSP(label,NN,JJ)    sensors[NN].pin_##label = PIN_SENSOR_##label##_##NN;
#define SSP2(NN)            if(NUM_SENSORS>NN) {  SSP(CSEL,NN,0)  SSP(CLK,NN,1)  SSP(MISO,NN,2)  SSP(MOSI,NN,3)  }
  SSP2(0)
  SSP2(1)
  SSP2(2)
  SSP2(3)
  SSP2(4)
  SSP2(5)

  for(ALL_SENSORS(i)) {
    sensors[i].start();
  }
  
  positionErrorFlags = 0;
  SET_BIT_ON(positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS);// | POSITION_ERROR_FLAG_ESTOP;

  // the first few reads will return junk so we force a couple empties here.
  update();
  update();
  
  reportDelay=0;
}


/**
 * Update sensorAngles with the latest values, adjust them by motors[i].angleHome, and cap them to [-180...180).
 */
void SensorManager::update() {
  sensorReady = false;
  uint16_t rawValue;
  int32_t steps[NUM_AXIES];
  float sensorAngles[NUM_SENSORS];
  float v;
  
  for(ALL_SENSORS(i)) {
    if(sensors[i].getRawValue(rawValue)) continue;
    v = extractAngleFromRawValue(rawValue);
    // Some of these are negative because the sensor is reading the opposite rotation from the Robot Overlord simulation.
    // Robot Overlord has the final say, so these are flipped to match the simulation.
    // This is the only place motor direction should ever be inverted.
    if(i!=1 && i!=2) v=-v;
    //Serial.print(motors[i].letter);
    //Serial.print("\traw=");  Serial.print(rawValue,BIN);
    //Serial.print("\tbefore=");  Serial.print(v);
    //Serial.print("\thome=");  Serial.print(sensors[i].angleHome);
    v -= sensors[i].angleHome;
    v = WRAP_DEGREES(v);
    //Serial.print("\tafter=");  Serial.println(v);
    sensorAngles[i] = v;
    sensors[i].angle = v;
  }
/*
  kinematics.anglesToSteps(sensorAngles, steps);

  for(ALL_SENSORS(i)){
    motors[i].stepsUpdated = steps[i];
  }*/
  
  sensorReady = true;

  if(TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_ERROR)) {
    if(TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR)) {
      Serial.println(F("\n\n** POSITION ERROR **\n"));
      CBI(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR);
    }
  } else {
    if(!TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR)) {
      SBI(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_FIRSTERROR);
    }
  }

  if(REPORT_ANGLES_CONTINUOUSLY) {
    if (millis() > reportDelay) {
      reportDelay = millis() + 50;
      parser.D17();
    }
  }
}
