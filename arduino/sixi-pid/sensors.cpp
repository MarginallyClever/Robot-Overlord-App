//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"

uint8_t sensorPins[4*NUM_SENSORS];
float sensorAngles[NUM_SENSORS];
uint8_t positionErrorFlags;
bool sensorReady;

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


/**
 * Update sensorAngles with the latest values, adjust them by motors[i].angleHome, and cap them to [-180...180).
 */
void sensorUpdate() {
  sensorReady = false;
  uint16_t rawValue;
  int32_t steps[NUM_MOTORS];
  float v;
  for(ALL_SENSORS(i)) {
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
    while(v>=180) v-=360;
    sensorAngles[i] = v;
  }
  parser.anglesToSteps(sensorAngles, steps);

  for(ALL_SENSORS(i)){
    motors[i].stepsUpdated = steps[i];
  }
    
}
