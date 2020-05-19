//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


SensorAS5147 sensors[NUM_SENSORS];
SensorManager sensorManager;


void SensorAS5147::setup() {
  pinMode(pins[SENSOR_CSEL],OUTPUT);
  pinMode(pins[SENSOR_CLK ],OUTPUT);  // clk
  pinMode(pins[SENSOR_MISO],INPUT);  // miso
  pinMode(pins[SENSOR_MOSI],OUTPUT);  // mosi
  
  digitalWrite(pins[SENSOR_CSEL],HIGH);  // csel
  digitalWrite(pins[SENSOR_MOSI],HIGH);  // mosi
}


/**
 * @param index the sensor to read
 * @param result where to store the returned value.  may be changed even if method fails.
 * @return 0 on fail, 1 on success.
// @see https://ams.com/documents/20143/36005/AS5147_DS000307_2-00.pdf
 */
bool SensorAS5147::getRawValue(uint16_t &result) {
  result=0;
  uint8_t input, parity=0;
  
  // Send the request for the angle value (command 0xFFFF)
  // at the same time as receiving an angle.

  // Collect the 16 bits of data from the sensor
  digitalWrite(pins[SENSOR_CSEL],LOW);
  
  for(int i=0;i<SENSOR_TOTAL_BITS;++i) {
    digitalWrite(pins[SENSOR_CLK],HIGH);  // clk
    // this is here to give a little more time to the clock going high.
    // only needed if the arduino is *very* fast.  I'm feeling generous.
    result <<= 1;
    digitalWrite(pins[SENSOR_CLK],LOW);  // clk
    
    input = digitalRead(pins[SENSOR_MISO]);  // miso
#ifdef VERBOSE
    Serial.print(input,DEC);
#endif
    result |= input;
    parity ^= (i>0) & input;
  }

  digitalWrite(pins[SENSOR_CSEL],HIGH);  // csel
  
  // check the parity bit
  return ( parity != (result>>SENSOR_DATA_BITS) );
}



void SensorManager::setup() {
  int i=0;

// SSP(CSEL,0) is equivalent to sensorPins[i++]=PIN_SENSOR_CSEL_0;
#define SSP(label,NN,JJ)    sensors[i].pins[JJ] = PIN_SENSOR_##label##_##NN;
#define SSP2(NN)            if(NUM_SENSORS>NN) {  SSP(CSEL,NN,0)  SSP(CLK,NN,1)  SSP(MISO,NN,2)  SSP(MOSI,NN,3)  }
  SSP2(0)
  SSP2(1)
  SSP2(2)
  SSP2(3)
  SSP2(4)
  SSP2(5)

  for(ALL_SENSORS(i)) {
    // MUST match the order in SSP2() above
    sensors[i].setup();
  }
  
  sensorManager.positionErrorFlags = 0;
  SET_BIT_ON(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS);// | POSITION_ERROR_FLAG_ESTOP;
}


/**
 * Update sensorAngles with the latest values, adjust them by motors[i].angleHome, and cap them to [-180...180).
 */
void SensorManager::update() {
  sensorReady = false;
  uint16_t rawValue;
  int32_t steps[NUM_MOTORS];
  float sensorAngles[NUM_SENSORS];
  float v;
  
  for(ALL_SENSORS(i)) {
    if(sensors[i].getRawValue(rawValue)) continue;
    v = extractAngleFromRawValue(rawValue);
    // Some of these are negative because the sensor is reading the opposite rotation from the Robot Overlord simulation.
    // Robot Overlord has the final say, so these are flipped to match the simulation.
    // This is the only place motor direction should ever be inverted.
    if(i!=1 && i!=2) v=-v;
    v -= motors[i].angleHome;
    v+=180;  // shift up so our desired range is 0...360
    v =fmod(v,360.0f);  // limit to within 0...360
    v-=180;  // now shift back to +/-180
    sensorAngles[i] = v;
    sensors[i].angle = v;
  }

  kinematics.anglesToSteps(sensorAngles, steps);

  for(ALL_SENSORS(i)){
    motors[i].stepsUpdated = steps[i];
  }
  
  sensorReady = true;
}
