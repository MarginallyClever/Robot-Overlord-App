//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


// GLOBALS

// Serial comm reception
#define FLAG_RELATIVE      (0)
#define RELATIVE_MOVES     (TEST(motionFlags,FLAG_RELATIVE))

Parser parser;


/**
 * Inverse Kinematics turns XY coordinates into step counts from each motor
 * This code is a duplicate of https://github.com/MarginallyClever/Robot-Overlord-App/blob/master/src/main/java/com/marginallyclever/robotOverlord/sixiRobot/java inverseKinematics()
 * @param angles the cartesian coordinate
 * @param steps a measure of each belt to that plotter position
*/
void IK(const float *const angles, int32_t *steps) {
  // each of the xyz motors are differential to each other.
  // to move only one motor means applying the negative of that value to the other two motors

  // consider a two motor differential: 
  // if x moves, subtract x from y.
  // if y moves, subtract y from x.
  // so for three axis,
  // for any axis N subtract the other two axies from this axis.

  // Some of these are negative because the motor is wired to turn the opposite direction from the Robot Overlord simulation.
  // Robot Overlord has the final say, so these are flipped to match the simulation.
  // This is the only place motor direction should ever be inverted.
  float J0 = -angles[0];  // anchor  (G0 X*)
  float J1 =  angles[1];  // shoulder (G0 Y*)
  float J2 =  angles[2];  // elbow (G0 Z*)
  float J3 = -angles[3];  // ulna  (G0 U*)
  float J4 =  angles[4];  // wrist (G0 V*)
  float J5 = -angles[5];  // hand  (G0 W*)

  // adjust for the wrist differential
  J5 += (J4/NEMA17_CYCLOID_GEARBOX_RATIO)+(J3/NEMA17_CYCLOID_GEARBOX_RATIO);
  J4 += (J3/NEMA17_CYCLOID_GEARBOX_RATIO);
  
  steps[0] = J0 * STEP_PER_DEGREES_0;  // ANCHOR
  steps[1] = J1 * STEP_PER_DEGREES_1;  // SHOULDER
  steps[2] = J2 * STEP_PER_DEGREES_2;  // ELBOW
  steps[3] = J3 * STEP_PER_DEGREES_3;  // ULNA
  steps[4] = J4 * STEP_PER_DEGREES_4;  // WRIST
  steps[5] = J5 * STEP_PER_DEGREES_5;  // HAND
  
  steps[NUM_MOTORS] = angles[6];
#ifdef DEBUG_IK
  Serial.print("J=");  Serial.print(J0);
  Serial.print('\t');  Serial.print(J1);
  Serial.print('\t');  Serial.print(J2);
  Serial.print('\t');  Serial.print(J3);
  Serial.print('\t');  Serial.print(J4);
  Serial.print('\t');  Serial.print(J5);
  Serial.print('\n');
#endif
}


/**
   Forward Kinematics - turns step counts into XY coordinates.  
   This code is a duplicate of https://github.com/MarginallyClever/Robot-Overlord-App/blob/master/src/main/java/com/marginallyclever/robotOverlord/sixiRobot/java forwardKinematics()
   @param steps a measure of each belt to that plotter position
   @param angles the resulting cartesian coordinate
   @return 0 if no problem, 1 on failure.
*/
int FK(uint32_t *steps, float *angles) {
  // TODO fill me in!

  return 0;
}


/**
   Look for character /code/ in the buffer and read the float that immediately follows it.
   @return the value found.  If nothing is found, /val/ is returned.
   @input code the character to look for.
   @input val the return value if /code/ is not found.
*/
float Parser::parseNumber(char code, float val) {
  char *ptr = serialBuffer; // start at the beginning of buffer
  char *finale = serialBuffer + sofar;
  for (ptr = serialBuffer; ptr < finale; ++ptr) { // walk to the end
    if (*ptr == ';') break;
    if (toupper(*ptr) == code) { // if you find code on your walk,
      return atof(ptr + 1); // convert the digits that follow into a float and return it
    }
  }
  return val;  // end reached, nothing found, return default val.
}


// @return 1 if the character is found in the serial buffer, 0 if it is not found.
uint8_t Parser::hasGCode(char code) {
  char *ptr = serialBuffer; // start at the beginning of buffer
  char *finale = serialBuffer + sofar;
  for (ptr = serialBuffer; ptr < finale; ++ptr) { // walk to the end
    if (*ptr == ';') break;
    if (toupper(*ptr) == code) { // if you find code on your walk,
      return 1;  // found
    }
  }
  return 0;  // not found
}


// @return 1 if CRC ok or not present, 0 if CRC check fails.
char Parser::checkLineNumberAndCRCisOK() {
  // is there a line number?
  int32_t cmd = parseNumber('N', -1);
  if (cmd != -1 && serialBuffer[0] == 'N') { // line number must appear first on the line
    if ( cmd != lineNumber ) {
      // wrong line number error
      Serial.print(F("BADLINENUM "));
      Serial.println(lineNumber);
      return 0;
    }

    // is there a checksum?
    int i;
    for (i = strlen(serialBuffer) - 1; i >= 0; --i) {
      if (serialBuffer[i] == '*') {
        break;
      }
    }

    if (i >= 0) {
      // yes.  is it valid?
      char checksum = 0;
      int c;
      for (c = 0; c < i; ++c) {
        checksum ^= serialBuffer[c];
      }
      c++; // skip *
      int against = strtod(serialBuffer + c, NULL);
      if ( checksum != against ) {
        Serial.print(F("BADCHECKSUM "));
        Serial.println(lineNumber);
        return 0;
      }
    } else {
      Serial.print(F("NOCHECKSUM "));
      Serial.println(lineNumber);
      return 0;
    }

    // remove checksum
    serialBuffer[i] = 0;

    lineNumber++;
  }

  return 1;  // ok!
}


/**
   prepares the input buffer to receive a new message and tells the serial connected device it is ready for more.
*/
void Parser::ready() {
  sofar = 0; // clear input buffer
  Serial.print(F("\n> "));  // signal ready to receive input
  lastCmdTimeMs = millis();
}


/**
 * process commands in the serial receive buffer
 */
void Parser::processCommand() {
  if( serialBuffer[0] == '\0' || serialBuffer[0] == ';' ) return;  // blank lines
  if(!checkLineNumberAndCRCisOK()) return; // message garbled
  
  // remove any trailing semicolon.
  int last = strlen(serialBuffer)-1;
  if( serialBuffer[last] == ';') serialBuffer[last]=0;
  
  if( !strncmp(serialBuffer, "UID", 3) ) {
    robot_uid = atoi(strchr(serialBuffer, ' ') + 1);
    eepromSaveUID();
  }

  int16_t cmd;

  // M commands
  cmd = parseNumber('M', -1);
  switch(cmd) {
    case 114:  M114();  break;
    case 206:  M206();  break;
    case 306:  M306();  break;
    case 428:  M428();  break;
    case 500:  M500();  break;
    case 501:  M501();  break;
    case 502:  M502();  break;
    case 503:  M503();  break;
    default:   break;
  }
  if (cmd != -1) return; // M command processed, stop.
  
  // D commands
  cmd = parseNumber('D', -1);
  switch(cmd) {
    case 10:  // get hardware version
      Serial.print(F("D10 V"));
      Serial.println(MACHINE_HARDWARE_VERSION);
      break;
    case 17:  D17();  break;
    case 18:  D18();  break;
    case 19:  positionErrorFlags ^= POSITION_ERROR_FLAG_CONTINUOUS;  break; // toggle
    case 20:  positionErrorFlags &= 0xffff ^ (POSITION_ERROR_FLAG_ERROR | POSITION_ERROR_FLAG_FIRSTERROR);  break; // off
    case 21:  positionErrorFlags ^= POSITION_ERROR_FLAG_ESTOP;  break; // toggle ESTOP
    default:  break;
  }
  if (cmd != -1) return; // D command processed, stop.

  // G commands
  cmd = parseNumber('G', lastGcommand);
  lastGcommand = -1;
  switch(cmd) {
    case  0:
    case  1:
      lastGcommand = cmd;
      G01();
      break;
    case 90:  G90();  break;
    case 91:  G91();  break;
    default: break;
  }
}


void Parser::update() {
  // listen for serial commands
  while(Serial.available() > 0) {
    char c = Serial.read();
    Serial.print(c);
    if (sofar < MAX_BUF) serialBuffer[sofar++] = c;
    if (c == '\r' || c == '\n') {
      serialBuffer[sofar - 1] = 0;

      // echo confirmation
      //Serial.println(serialBuffer);

      // do something with the command
      processCommand();
      ready();
    }
  }
}



// M COMMANDS



/**
 * M114
 * Print the current target position
 */
void Parser::M114() {
  Serial.print(F("M114"));
  for(ALL_MOTORS(i)) {
    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleTarget);
  }

//Serial.print(F(" F"));  Serial.print(feed_rate);
//Serial.print(F(" A"));  Serial.print(acceleration);
  Serial.println();
}


/**
 * M206 set home offsets
 */
void Parser::M206() {
  // cancel the current home offsets
  for(ALL_MOTORS(i)) {
    float angleHome = parseNumber( motors[i].letter, motors[i].angleHome );
    motors[i].angleHome = min(max(angleHome,360),-360);
  }
}


/**
 * M306 adjust PID
 */
void Parser::M306() {
  if(hasGCode('L')) {
    int axis = parseNumber('L',0);
    axis = max(min(axis,5),0);
    
      float p = parseNumber('P', motors[axis].kp );
      float i = parseNumber('I', motors[axis].ki );
      float d = parseNumber('D', motors[axis].kd );  // this only works as long as M codes are processed before D codes.
    
    // disable global interrupts
    CRITICAL_SECTION_START();
      motors[axis].setPID(p,i,d);
    // enable global interrupts
    CRITICAL_SECTION_END();
    
    // report values
    Serial.print("M306 ");
    Serial.print(motors[axis].letter);
    Serial.print(" = ");
    Serial.print(p,6);
    Serial.print(",");
    Serial.print(i,6);
    Serial.print(",");
    Serial.print(d,6);
  }
}

// M428 - set home position to the current angle values
void Parser::M428() {
  // cancel the current home offsets
  M502();
  
  // read the sensor
  sensorUpdate();
  
  // apply the new offsets
  for(ALL_MOTORS(i)) {
    motors[i].angleHome = sensorAngles[i];
  }
  D18();
}


// M500 - save home offsets
void Parser::M500() {
  eepromSaveHome();
}


// M501 - load home offsets
void Parser::M501() {
  eepromLoadHome();
}


// M502 - reset the home offsets
void Parser::M502() {
  for(ALL_MOTORS(i)) {
    motors[i].angleHome = 0;
  }
  D18();
}


// M503 - report the home offsets
void Parser::M503() {
  Serial.print(F("M503"));
  for(ALL_MOTORS(i)) {
    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleHome);
  }
  Serial.println();
}




// D COMMANDS




/**
 * D17 report the 6 axis sensor values from the Sixi robot arm.
 */
void Parser::D17() {
  Serial.print(F("D17"));
  for(ALL_MOTORS(i)) {
    Serial.print(' ');
    Serial.print(sensorAngles[i], 2);
  }

  Serial.print('\t');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_CONTINUOUS)!=0)?'+':'-');
  Serial.print(((positionErrorFlags & POSITION_ERROR_FLAG_ERROR) != 0) ? '+' : '-');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_FIRSTERROR)!=0)?'+':'-');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_ESTOP)!=0)?'+':'-');
  Serial.println();
}


// D18 copy sensor values to motor step positions.
// get the average of a few samples.
void Parser::D18() {
  float angles[NUM_SENSORS];
  int numSamples = 10;

  for(ALL_SENSORS(i)) angles[i] = 0;

  CRITICAL_SECTION_START();

  for(int i = 0; i < numSamples; ++i) {
    sensorUpdate();
    for(ALL_SENSORS(j)) {
      angles[j] += sensorAngles[j];
    }
  }
  
  for(ALL_SENSORS(i)) {
    angles[i] /= (float)numSamples;
  }

  int32_t steps[NUM_MOTORS];
  IK(angles,steps);
  
  for(ALL_SENSORS(i)) {
    motors[i].angleTarget = angles[i];
    motors[i].stepsNow    = steps[i];
    motors[i].stepsTarget = steps[i];
  }
  
  CRITICAL_SECTION_END();
}




// G COMMANDS




/**
 * G0/G1 linear moves
 */
void Parser::G01() {
  float angles[NUM_MOTORS];
  int32_t steps[NUM_MOTORS];

  Serial.println( RELATIVE_MOVES ? "REL" : "ABS" );
  
  for(ALL_MOTORS(i)) {
    float start = RELATIVE_MOVES ? 0 : motors[i].angleTarget;
    
    float parsed = (int32_t)floor(parseNumber( motors[i].letter, start ));
    
    angles[i] = RELATIVE_MOVES ? angles[i] + parsed : parsed;
  }
  
  IK(angles,steps);

  CRITICAL_SECTION_START();
  for(ALL_MOTORS(i)) {
//*
    Serial.println(motors[i].letter);
    Serial.print("\tangleTarget0=");
    Serial.println(motors[i].angleTarget);
    Serial.print("\tstepsTarget0=");
    Serial.println(motors[i].stepsTarget);
    Serial.print("\tangleTarget1=");
    Serial.println(angles[i]);
    Serial.print("\tstepsTarget1=");
    Serial.println(steps[i]);
    Serial.print("\tstepsNow=");
    Serial.println(motors[i].stepsNow);
//*/
    motors[i].angleTarget = angles[i];
    motors[i].stepsTarget = steps[i];
  }
  CRITICAL_SECTION_END();
}


void Parser::G90() {
  SET_BIT(motionFlags,FLAG_RELATIVE);
}


void Parser::G91() {
  UNSET_BIT(motionFlags,FLAG_RELATIVE);
}

