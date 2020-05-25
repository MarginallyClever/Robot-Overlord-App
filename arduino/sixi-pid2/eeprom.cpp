//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint32_t robot_uid;


extern Eeprom eeprom;


// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
long Eeprom::readLong(int ee) {
  long value = 0;
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


// 2020-01-31 Dan added check to not update EEPROM if value is unchanged.
// from http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1234477290/3
// returns true if the value was changed.
bool Eeprom::writeLong(int ee, long value) {
  if(readLong(ee) == value) return false;
  
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);

  return true;
}


float readFloat(int ee) {
  float value = 0;
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  *p++ = EEPROM.read(ee++);
  return value;
}


// returns true if the value was changed.
bool writeFloat(int ee, float value) {
  if(readFloat(ee) == value) return false;
  
  byte* p = (byte*)(void*)&value;
  for (uint8_t i = 0; i < sizeof(value); i++)
  EEPROM.write(ee++, *p++);

  return true;
}


uint8_t Eeprom::loadVersion() {
  return EEPROM.read(ADDR_VERSION);
}

void Eeprom::saveUID() {
  Serial.println(F("Saving UID."));
  writeLong(ADDR_UUID,(uint32_t)robot_uid);
}

uint8_t Eeprom::loadUID() {
  return EEPROM.read(ADDR_VERSION);
}

void Eeprom::saveLimits() {
  Serial.println(F("Saving limits."));
  int j=ADDR_LIMITS;
  for(ALL_AXIES(i)) {
    writeFloat(j,motors[i].limitMax);
    j+=4;
    writeFloat(j,motors[i].limitMin);
    j+=4;
  }
}

void Eeprom::loadLimits() {
  int j=ADDR_LIMITS;
  for(ALL_AXIES(i)) {
    motors[i].limitMax = (float)readFloat(j);
    j+=4;
    motors[i].limitMin = (float)readFloat(j);
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
void Eeprom::adjustLimits(float *limits) {
  Serial.println(F("Adjusting limits."));
  int j=0;
  int changed=0;
  float v;
  for(ALL_AXIES(i)) {
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

void Eeprom::saveHome() {
  Serial.print(F("Saving home:"));
  int j=ADDR_HOME;
  // this loop must not be more than NUM_AXIES
  for(ALL_SENSORS(i)) {
    writeLong(j,sensorManager.sensors[i].angleHome*100.0f);
    j+=4;

    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(sensorManager.sensors[i].angleHome);
  }
  Serial.println();
}

void Eeprom::loadHome() {
  //Serial.print(F("Loading home:"));
  int j=ADDR_HOME;
  // this loop must not be more than NUM_AXIES
  for(ALL_SENSORS(i)) {
    sensorManager.sensors[i].angleHome = (float)readLong(j)/100.0f;
    //Serial.print(' ');
    //Serial.print(motors[i].letter);
    //Serial.print(motors[i].angleHome);
    j+=4;
  }
  //Serial.println();
}

void Eeprom::saveAll() {
  saveUID();
  saveLimits();
  saveHome();
}


void Eeprom::loadAll() {
  char versionNumber = loadVersion();
  if( versionNumber != FIRMWARE_VERSION ) {
    // If not the current FIRMWARE_VERSION or the FIRMWARE_VERSION is sullied (i.e. unknown data)
    // Update the version number
    EEPROM.write(ADDR_VERSION,FIRMWARE_VERSION);
  }
  
  // Retrieve stored configuration
  robot_uid=readLong(ADDR_UUID);
  loadLimits();
  loadHome();
  loadPID();
}


void Eeprom::savePID() {
  int j=ADDR_PID;
  
  for(ALL_MOTORS(i)) {
    writeFloat(j,motors[i].kp);
    j+=4;
    writeFloat(j,motors[i].ki);
    j+=4;
    writeFloat(j,motors[i].kd);
    j+=4;
  }
}

void Eeprom::loadPID() {
  int j=ADDR_PID;
  for(ALL_MOTORS(i)) {

    motors[i].kp = readFloat(j);
    motors[i].ki = readFloat(j+4);
    motors[i].kd = readFloat(j+8);
    j+=12;
  }
}
