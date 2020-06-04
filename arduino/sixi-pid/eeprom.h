#pragma once

// EEPROM - should exactly match makelangelo-firmware eeprom for legacy reasons


#define FIRMWARE_VERSION        10    // Increment when adding new variables
#define SIZEOF_FLOAT_BYTES      (4)
#define SIZEOF_LONG_BYTES       (4)
#define ADDR_VERSION            0                          // 0..255 (1 byte)
#define ADDR_UUID               (ADDR_VERSION+1)
#define EEPROM_UUID_LENGTH      (SIZEOF_LONG_BYTES)

#define ADDR_LIMITS             (ADDR_UUID+EEPROM_UUID_LENGTH)
#define EEPROM_LIMITS_LENGTH    (NUM_AXIES*2*SIZEOF_FLOAT_BYTES)

#define ADDR_HOME               (ADDR_LIMITS+EEPROM_LIMITS_LENGTH)
#define EEPROM_HOME_LENGTH      (NUM_AXIES*1*SIZEOF_FLOAT_BYTES)

#define ADDR_CALIBRATION_LEFT   (ADDR_HOME+EEPROM_HOME_LENGTH)
#define ADDR_CALIBRATION_LENGTH (1*SIZEOF_FLOAT_BYTES)
#define ADDR_CALIBRATION_RIGHT  (ADDR_CALIBRATION_LEFT+ADDR_CALIBRATION_LENGTH)

#define ADDR_PID                (ADDR_CALIBRATION_RIGHT+ADDR_CALIBRATION_LENGTH)
#define EEPROM_PID_LENGTH       (NUM_AXIES*3*SIZEOF_FLOAT_BYTES)


extern uint32_t robot_uid;

class Eeprom {
public:
  long readLong(int ee);
  boolean writeLong(int ee, long value);
  
  uint8_t loadVersion();
  void saveUID();
  uint8_t loadUID();
  void saveLimits();
  void loadLimits();

  /**
   * @param limits NUM_MOTORS*2 floats.  Each pair is one float for max limit and one for min limit.
   */
  void adjustLimits(float *limits);
  
  void saveHome();
  void loadHome();

  void saveAll();
  void loadAll();
  
  void savePID();
  void loadPID();
};

extern Eeprom eeprom;
