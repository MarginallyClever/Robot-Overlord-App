//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint32_t robot_uid;



// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
uint32_t EEPROM_readLong(int ee) {
  uint32_t value = 0;
  byte* p = (byte*)(void*)&value;
  for (uint16_t i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


// 2020-01-31 Dan added check to not update EEPROM if value is unchanged.
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
// returns true if the value was changed.
bool EEPROM_writeLong(int ee, uint32_t value) {
  if(EEPROM_readLong(ee) == value) return false;
  
  byte* p = (byte*)(void*)&value;
  for (uint16_t i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);

  return true;
}


/**
 * 
 */
char loadVersion() {
  return EEPROM.read(ADDR_VERSION);
}


/**
 * 
 */
void saveUID() {
  Serial.println(F("Saving UID."));
  EEPROM_writeLong(ADDR_UUID,(uint32_t)robot_uid);
}


/**
 * 
 */
void saveLimits() {
  Serial.println(F("Saving limits."));
  int i,j=ADDR_LIMITS;
  for(i=0;i<NUM_MOTORS;++i) {
    EEPROM_writeLong(j,motors[i].limitMax*100);
    j+=4;
    EEPROM_writeLong(j,motors[i].limitMin*100);
    j+=4;
  }
}


/**
 * 
 */
void loadLimits() {
  int i,j=ADDR_LIMITS;
  for(i=0;i<NUM_MOTORS;++i) {
    motors[i].limitMax = (float)EEPROM_readLong(j)/100.0f;
    j+=4;
    motors[i].limitMin = (float)EEPROM_readLong(j)/100.0f;
    j+=4;
    //Serial.print("Axis ");
    //Serial.print(i);
    //Serial.print(" Min ");
    //Serial.print(axies[i].limitMin);
    //Serial.print(" Max ");
    //Serial.print(axies[i].limitMax);
    //Serial.println();
  }
}


/**
 * @param limits NUM_MOTORS*2 floats.  Each pair is one float for max limit and one for min limit.
 */
void adjustLimits(float *limits) {
  Serial.println(F("Adjusting limits."));
  int i,j=0;
  int changed=0;
  float v;
  for(i=0;i<NUM_MOTORS;++i) {
    // max test
    v = floor(limits[j]*100.0f)/100.0f;
    if(v != motors[i].limitMax) {
      motors[i].limitMax = v;
      changed=1;
    }
    j++;
    // min test
    v = floor(limits[j]*100.0f)/100.0f;
    if(v != motors[i].limitMin) {
      motors[i].limitMin = v;
      changed=1;
    }
    j++;
  }

  if( changed != 0 ) {
    saveLimits();
  }
}


/**
 * 
 */
void saveHome() {
  Serial.println(F("Saving home."));
  int i,j=ADDR_HOME;
  for(i=0;i<NUM_MOTORS;++i) {
    EEPROM_writeLong(j,(uint32_t)(motors[i].angleHome*100.0f));
    j+=4;
  }
}


/**
 * 
 */
void loadHome() {
  int i,j=ADDR_HOME;
  for(i=0;i<NUM_MOTORS;++i) {
    motors[i].angleHome = (float)EEPROM_readLong(j)/100.0f;
    j+=4;
  }
}


/**
 * 
 */
void loadConfig() {
  char versionNumber = loadVersion();
  if( versionNumber != FIRMWARE_VERSION ) {
    // If not the current FIRMWARE_VERSION or the FIRMWARE_VERSION is sullied (i.e. unknown data)
    // Update the version number
    EEPROM.write(ADDR_VERSION,FIRMWARE_VERSION);
  }
  
  // Retrieve stored configuration
  robot_uid=EEPROM_readLong(ADDR_UUID);
  loadLimits();
  loadHome();
}
