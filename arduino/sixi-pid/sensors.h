#pragma once


// SENSORS
#ifndef NUM_SENSORS
#error "NUM_SENSORS undefined"
#endif

// use in for(ALL_SENSORS(i)) { //i will be rising
#define ALL_SENSORS(NN) int NN=0;NN<NUM_SENSORS;++NN

// sensor bits, flags, and masks
#define BOTTOM_14_MASK       (0x3FFF)
#define SENSOR_TOTAL_BITS    (16)
#define SENSOR_DATA_BITS     (15)
#define SENSOR_ANGLE_BITS    (14)
#define SENSOR_ANGLE_PER_BIT (360.0/(float)((uint32_t)1<<SENSOR_ANGLE_BITS))  // 0.02197265625

// behaviour flags
#define POSITION_ERROR_FLAG_CONTINUOUS   (0)  // report position (d17) continuously?
#define POSITION_ERROR_FLAG_ERROR        (1)  // has error occurred?
#define POSITION_ERROR_FLAG_FIRSTERROR   (2)  // report the error once per occurrence
#define POSITION_ERROR_FLAG_ESTOP        (3)  // check for error at all?

#define REPORT_ANGLES_CONTINUOUSLY (TEST(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS))


class SensorAS5147 {
public:
  uint8_t pin_CSEL;
  uint8_t pin_CLK;
  uint8_t pin_MISO;
  uint8_t pin_MOSI;
  float angle; // current reading after adjustment
  float angleHome;  // sensor raw angle value at home position.  reading - this = 0.

  void start();
  
  /**
   * See https://ams.com/documents/20143/36005/AS5147_DS000307_2-00.pdf
   * @param result where to store the returned value.  may be changed even if method fails.
   * @return 0 on fail, 1 on success.
   */
  bool getRawValue(uint16_t &result);
};


class SensorManager {
public:
  SensorAS5147 sensors[NUM_SENSORS];
  uint8_t positionErrorFlags;

  void setup();
  
  // Update sensorAngles with the latest values, adjust them by motors[i].angleHome, and cap them to [-180...180).
  void update();
  
  /**
   * @param rawValue 16 bit value from as4157 sensor, including parity and EF bit
   * @return degrees calculated from bottom 14 bits.
   */
  inline float extractAngleFromRawValue(uint16_t rawValue) {
    return (float)(rawValue & BOTTOM_14_MASK) * 360.0 / (float)(1<<SENSOR_ANGLE_BITS);
  }
};


extern SensorManager sensorManager;
