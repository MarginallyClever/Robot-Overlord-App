#pragma once


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


extern uint32_t robot_uid;


/**
 * 
 */
char loadVersion();

/**
 * 
 */
extern void saveUID();

/**
 * 
 */
void saveLimits();

/**
 * 
 */
void loadLimits();

/**
 * @param limits NUM_MOTORS*2 floats.  Each pair is one float for max limit and one for min limit.
 */
void adjustLimits(float *limits);

/**
 * 
 */
void saveHome();

/**
 * 
 */
void loadHome();

/**
 * 
 */
extern void loadConfig();
