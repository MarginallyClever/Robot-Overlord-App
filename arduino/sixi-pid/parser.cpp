//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


// GLOBALS

// Serial comm reception
char serialBuffer[MAX_BUF + 1]; // Serial buffer
int sofar;                      // Serial buffer progress
uint32_t lastCmdTimeMs;         // prevent timeouts
int32_t lineNumber = 0;        // make sure commands arrive in order
uint8_t lastGcommand = -1;
uint32_t reportDelay = 0;  // how long since last D17 sent out




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
float parseNumber(char code, float val) {
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


/**
   @return 1 if the character is found in the serial buffer, 0 if it is not found.
*/
char hasGCode(char code) {
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


/**
   @return 1 if CRC ok or not present, 0 if CRC check fails.
*/
char checkLineNumberAndCRCisOK() {
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
 * D22
 * reset home position to the current angle values.
 */
void sixiResetSensorOffsets() {
  int i;
  // cancel the current home offsets
  for (i = 0; i < NUM_SENSORS; ++i) {
    motors[i].angleHome = 0;
  }
  // read the sensor
  sensorUpdate();
  // apply the new offsets
  for (i = 0; i < NUM_SENSORS; ++i) {
    motors[i].angleHome = sensorAngles[i];
  }
}

/**
   M114
   Print the X,Y,Z, feedrate, acceleration, and home position
*/
void where() {
  int i;
  for (i = 0; i < NUM_MOTORS; ++i) {
    Serial.print(motors[i].letter);
    Serial.print(motors[i].getDegrees());
    Serial.print(' ');
  }

//Serial.print('F');  Serial.print(feed_rate);  Serial.print(' ');
//Serial.print(F('A'));  Serial.print(acceleration);
  Serial.println();
}

/**
 * D17 report the 6 axis sensor values from the Sixi robot arm.
 */
void reportAllAngleValues() {
  Serial.print(F("D17"));
  for (int i = 0; i < NUM_MOTORS; ++i) {
    Serial.print('\t');
    Serial.print(sensorAngles[i], 2);
  }
  /*
    if(current_segment==last_segment) {
    // report estimated position
    Serial.print(F("\t-\t"));

    working_seg = get_current_segment();
    for (uint8_t i = 0; i < NUM_SENSORS; ++i) {
      //float diff = working_seg->a[i].expectedPosition - sensorAngles[i];
      //Serial.print('\t');
      //Serial.print(abs(diff),3);
      Serial.print('\t');
      Serial.print(working_seg->a[i].expectedPosition,2);
    }
    }*/

  Serial.print('\t');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_CONTINUOUS)!=0)?'+':'-');
  Serial.print(((positionErrorFlags & POSITION_ERROR_FLAG_ERROR) != 0) ? '+' : '-');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_FIRSTERROR)!=0)?'+':'-');
  //Serial.print(((positionErrorFlags&POSITION_ERROR_FLAG_ESTOP)!=0)?'+':'-');
  Serial.println();
}


/**
 * D18 copy sensor values to motor step positions.
 */
void copySensorsToMotorPositions() {
  float a[NUM_MOTORS];
  int i, j;
  int numSamples = 10;

  for (j = 0; j < NUM_MOTORS; ++j) a[j] = 0;

  // assert(NUM_SENSORS <= NUM_MOTORS);

  for (i = 0; i < numSamples; ++i) {
    sensorUpdate();
    for (j = 0; j < NUM_SENSORS; ++j) {
      a[j] += sensorAngles[j];
    }
  }
  for (j = 0; j < NUM_SENSORS; ++j) {
    motors[i].stepsNow = a[j] / (float)numSamples;
  }
}


/**
 * D50 adjust PID
 */
void parsePID() {
  if(hasGCode('L')) {
    int axis = parseNumber('L',0);
    axis = max(min(axis,5),0);
    
    // disable global interrupts
    CRITICAL_SECTION_START();
      float p = parseNumber('P', motors[axis].kp );
      float i = parseNumber('I', motors[axis].ki );
      float d = parseNumber('E', motors[axis].kd );
    
      motors[axis].setPID(p,i,d);
    // enable global interrupts
    CRITICAL_SECTION_END();
    
    // report values
    Serial.print("PID ");
    Serial.print(motors[axis].letter);
    Serial.print(" = ");
    Serial.print(p,6);
    Serial.print(",");
    Serial.print(i,6);
    Serial.print(",");
    Serial.print(d,6);
  }
}

/**
   prepares the input buffer to receive a new message and tells the serial connected device it is ready for more.
*/
void parserReady() {
  sofar = 0; // clear input buffer
  Serial.print(F("\n> "));  // signal ready to receive input
  lastCmdTimeMs = millis();
}


/**
 * G0/G1 linear moves
 */
void parseLine() {
  float angles[NUM_MOTORS];
  int32_t steps[NUM_MOTORS];

  Serial.println();
  
  for(ALL_MOTORS(i)) {
    float parsed = parseNumber( motors[i].letter, motors[i].angleTarget );
    angles[i] = (int32_t)floor(parsed);
  }
  
  IK(angles,steps);

  CRITICAL_SECTION_START();
  for(ALL_MOTORS(i)) {
/*
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleTarget);
    Serial.print('\t');
    Serial.print(motors[i].stepsTarget);
    Serial.print('\t');
    Serial.print(angles[i]);
    Serial.print('\t');
    Serial.print(steps[i]);
    Serial.print('\t');
    Serial.print(motors[i].stepsNow);
    Serial.println();
//*/
    motors[i].stepsTarget = steps[i];
  }
  CRITICAL_SECTION_END();
}


/**
 * process commands in the serial receive buffer
 */
void processCommand() {
  if( serialBuffer[0] == '\0' || serialBuffer[0] == ';' ) return;  // blank lines
  if(!checkLineNumberAndCRCisOK()) return; // message garbled
  
  // remove any trailing semicolon.
  int last = strlen(serialBuffer)-1;
  if( serialBuffer[last] == ';') serialBuffer[last]=0;
  
  if( !strncmp(serialBuffer, "UID", 3) ) {
    robot_uid = atoi(strchr(serialBuffer, ' ') + 1);
    saveUID();
  }

  int8_t cmd;

  // M codes
  cmd = parseNumber('M', -1);
  switch (cmd) {
    case 114:  where();  break;
    default:   break;
  }
  if (cmd != -1) return; // M command processed, stop.
  
  // machine style-specific codes
  cmd = parseNumber('D', -1);
  switch (cmd) {
    case 10:  // get hardware version
      Serial.print(F("D10 V"));
      Serial.println(MACHINE_HARDWARE_VERSION);
      break;
    case 17:  reportAllAngleValues();  break;
    case 18:  copySensorsToMotorPositions();  break;
    case 19:  positionErrorFlags ^= POSITION_ERROR_FLAG_CONTINUOUS;  break; // toggle
    case 20:  positionErrorFlags &= 0xffff ^ (POSITION_ERROR_FLAG_ERROR | POSITION_ERROR_FLAG_FIRSTERROR);  break; // off
    case 21:  positionErrorFlags ^= POSITION_ERROR_FLAG_ESTOP;  break; // toggle ESTOP
    case 22:  sixiResetSensorOffsets();  break;
    case 50:  parsePID();  break;
    default:  break;
  }
  if (cmd != -1) return; // D command processed, stop.

  // no M or D commands were found.  This is probably a G-command.
  // G codes
  cmd = parseNumber('G', lastGcommand);
  lastGcommand = -1;
  switch (cmd) {
    case  0:
    case  1:
      lastGcommand = cmd;
      parseLine();
      break;
    default: break;
  }
}


void serialUpdate() {
  // listen for serial commands
  while (Serial.available() > 0) {
    char c = Serial.read();
    Serial.print(c);
    if (sofar < MAX_BUF) serialBuffer[sofar++] = c;
    if (c == '\r' || c == '\n') {
      serialBuffer[sofar - 1] = 0;

      // echo confirmation
      //Serial.println(serialBuffer);

      // do something with the command
      processCommand();
      parserReady();
    }
  }
}
