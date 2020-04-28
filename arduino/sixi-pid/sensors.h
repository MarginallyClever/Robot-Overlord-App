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
#define SENSOR_ANGLE_PER_BIT (360.0/(float)((uint32_t)1<<SENSOR_ANGLE_BITS))  // 0.00549316406

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
