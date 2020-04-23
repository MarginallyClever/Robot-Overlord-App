#pragma once


// SENSORS
#define NUM_SENSORS          (6)

// use in for(ALL_SENSORS(i)) { //i will be rising
#define ALL_SENSORS(NN) int NN=0;NN<NUM_SENSORS;++NN

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

// behaviour flags
#define POSITION_ERROR_FLAG_CONTINUOUS   (1<<0)  // report position (d17) continuously?
#define POSITION_ERROR_FLAG_ERROR        (1<<1)  // has error occurred?
#define POSITION_ERROR_FLAG_FIRSTERROR   (1<<2)  // report the error once per occurrence
#define POSITION_ERROR_FLAG_ESTOP        (1<<3)  // check for error at all?



extern uint8_t sensorPins[4*NUM_SENSORS];
extern float sensorAngles[NUM_SENSORS];
extern uint8_t positionErrorFlags;
extern bool sensorReady;


/**
 * Update sensorAngles with the latest values, adjust them by motors[i].angleHome, and cap them to [-180...180).
 */
void sensorUpdate();
