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
  for (uint8_t i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


// 2020-01-31 Dan added check to not update EEPROM if value is unchanged.
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
// returns true if the value was changed.
bool EEPROM_writeLong(int ee, uint32_t value) {
  if(EEPROM_readLong(ee) == value) return false;
  
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);

  return true;
}


float EEPROM_readFloat(int ee) {
  float value = 0;
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


// returns true if the value was changed.
bool EEPROM_writeFloat(int ee, float value) {
  if(EEPROM_readFloat(ee) == value) return false;
  
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);

  return true;
}


char eepromLoadVersion() {
  return EEPROM.read(ADDR_VERSION);
}

void eepromSaveUID() {
  Serial.println(F("Saving UID."));
  EEPROM_writeLong(ADDR_UUID,(uint32_t)robot_uid);
}

char eepromLoadUID() {
  return EEPROM.read(ADDR_VERSION);
}

void eepromSaveLimits() {
  Serial.println(F("Saving limits."));
  int j=ADDR_LIMITS;
  for(ALL_MOTORS(i)) {
    EEPROM_writeFloat(j,motors[i].limitMax);
    j+=4;
    EEPROM_writeFloat(j,motors[i].limitMin);
    j+=4;
  }
}

void eepromLoadLimits() {
  int j=ADDR_LIMITS;
  for(ALL_MOTORS(i)) {
    motors[i].limitMax = (float)EEPROM_readFloat(j);
    j+=4;
    motors[i].limitMin = (float)EEPROM_readFloat(j);
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
void eepromAdjustLimits(float *limits) {
  Serial.println(F("Adjusting limits."));
  int j=0;
  int changed=0;
  float v;
  for(ALL_MOTORS(i)) {
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
    eepromSaveLimits();
  }
}

void eepromSaveHome() {
  Serial.println(F("Saving home:"));
  int j=ADDR_HOME;
  for(ALL_MOTORS(i)) {
    EEPROM_writeFloat(j,motors[i].angleHome);
    j+=4;

    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleHome);
  }
  Serial.println();
}

void eepromLoadHome() {
  Serial.println(F("Loading home:"));
  int j=ADDR_HOME;
  for(ALL_MOTORS(i)) {
    motors[i].angleHome = EEPROM_readFloat(j);
    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleHome);
    
    j+=4;
  }
  Serial.println();
}

void eepromSaveAll() {
  eepromSaveUID();
  eepromSaveLimits();
  eepromSaveHome();
}

void eepromLoadAll() {
  char versionNumber = eepromLoadVersion();
  if( versionNumber != FIRMWARE_VERSION ) {
    // If not the current FIRMWARE_VERSION or the FIRMWARE_VERSION is sullied (i.e. unknown data)
    // Update the version number
    EEPROM.write(ADDR_VERSION,FIRMWARE_VERSION);
  }
  
  // Retrieve stored configuration
  robot_uid=EEPROM_readLong(ADDR_UUID);
  eepromLoadLimits();
  eepromLoadHome();
}

void eepromSavePID() {
  Serial.println(F("Saving PID values:"));
  int j=ADDR_PID;
  
  for(ALL_MOTORS(i)) {
    EEPROM_writeFloat(j,motors[i].kp);
    EEPROM_writeFloat(j+4,motors[i].ki);
    EEPROM_writeFloat(j+8,motors[i].kd);
    j+=12;

    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(' P');
    Serial.print(motors[i].kp);
    Serial.print(' I');
    Serial.print(motors[i].ki);
    Serial.print(' D');
    Serial.print(motors[i].kd);
  }
  Serial.println();
}

void eepromLoadPID() {
  Serial.println(F("Loading PID values:"));
  int j=ADDR_PID;
  for(ALL_MOTORS(i)) {

    motors[i].kp = EEPROM_readFloat(j);
    motors[i].ki = EEPROM_readFloat(j+4);
    motors[i].kd = EEPROM_readFloat(j+8);
    j+=12;

    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(' P');
    Serial.print(motors[i].kp);
    Serial.print(' I');
    Serial.print(motors[i].ki);
    Serial.print(' D');
    Serial.print(motors[i].kd);
  }
  Serial.println();
}
