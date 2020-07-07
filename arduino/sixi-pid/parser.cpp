//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


// GLOBALS

Parser parser;


void Parser::setup() {
  Serial.begin(BAUD);
  // clear input buffer
  sofar = 0;
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
  if (serialBuffer[0] == 'N') { // line number must appear first on the line
    int32_t cmd = parseNumber('N', -1);
    if( cmd != lineNumber ) {
      // wrong line number error
      Serial.print(F("BADLINENUM "));
      Serial.println(lineNumber);
      return 0;
    }

    // next time around, wait for the next line number.
    lineNumber++;
  }

  // is there a checksum?
  int found=-1;
  int i;
  for (i = strlen(serialBuffer) - 1; i >= 0; --i) {
    if (serialBuffer[i] == '*') {
      found=i;
      break;
    }
  }

  if(found==-1) {
    Serial.print("NOCHECKSUM");
    Serial.println(serialBuffer);
    return 0;
  }
  
  // yes.  is it valid?
  int checksum = 0;
  for(int c = 0; c < i; ++c) {
    checksum = ( checksum ^ serialBuffer[c] ) & 0xFF;
  }
  int against = strtod(serialBuffer + i + 1, NULL);
  if ( checksum != against ) {
    Serial.print("BADCHECKSUM calc=");
    Serial.print(checksum);
    Serial.print(" sent=");
    Serial.println(against);
    return 0;
  }

  // remove checksum
  serialBuffer[i] = 0;
 
  return 1;  // ok!
}


/**
   prepares the input buffer to receive a new message and tells the serial connected device it is ready for more.
*/
void Parser::ready() {
  Serial.print(F("\r\n> "));  // signal ready to receive input
  lastCmdTimeMs = millis();
}


void Parser::update() {
  // listen for serial commands
  if(Serial.available() > 0) {
    char c = Serial.read();
    //Serial.print(c);
    if(sofar < MAX_BUF) serialBuffer[sofar++] = c;
    if (c == '\r' || c == '\n') {
      serialBuffer[sofar - 1] = 0;

      // echo confirmation
      if(MUST_ECHO) {
        Serial.println(serialBuffer);
      }

      // start to measure time
     // uint32_t startTime_processCommand = micros();

      // do something with the command
      processCommand();
/*
      // finish measuring
      uint32_t endTime_processCommand = micros();
      uint32_t dt_processCommand = endTime_processCommand - startTime_processCommand;
      dtBuffer_processTime[dtBufferI_processTime]=dt_processCommand;
      uint32_t rollingSum = 0;
      for(int i=0;i<PARSER_DT_BUFFER_LEN;++i) {
        rollingSum += dtBuffer_processTime[i];
      }
      rollingAvg = (float)rollingSum/PARSER_DT_BUFFER_LEN;
      dtBufferI_processTime = (dtBufferI_processTime+1) % PARSER_DT_BUFFER_LEN;
      
      Serial.println(rollingAvg);
      */
      // clear input buffer
      sofar = 0;
      // go again
      ready();
    }
  }
}


/**
   process commands in the serial receive buffer
*/
void Parser::processCommand() {
  if( serialBuffer[0] == '\0' || serialBuffer[0] == ';' ) return; // blank lines

  if(IS_STRICT && !checkLineNumberAndCRCisOK()) return; // message garbled

  // remove any trailing semicolon.
  int last = strlen(serialBuffer) - 1;
  if( serialBuffer[last] == ';') serialBuffer[last] = 0;

  if( !strncmp(serialBuffer, "UID", 3) ) {
    robot_uid = atoi(strchr(serialBuffer, ' ') + 1);
    eeprom.saveUID();
  }

  int16_t cmd;

  // M commands
  if(hasGCode('M')) {
    cmd = parseNumber('M', -1);
    switch (cmd) {
      case 110:  M110();  break;
      case 111:  M111();  break;
      case 112:  M112();  break;
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
  }
  
  // machine style-specific codes
  if(hasGCode('D')) {
    cmd = parseNumber('D', -1);
    switch (cmd) {
      case 10:  // get hardware version
        Serial.print(F("D10 V"));
        Serial.println(MACHINE_HARDWARE_VERSION);
        break;
      case 17:  D17();  break;
      case 18:  D18();  break;
      case 19:  D19();  break;
      case 20:  D20();  break;
      case 21:  D21();  break;
      case 22:  D22();  break;
      case 50:  D50();  break;
      default:  break;
    }
    if (cmd != -1) return; // D command processed, stop.
  }
  
  // G commands
  cmd = parseNumber('G', lastGcommand);
  lastGcommand = -1;
  switch (cmd) {
    case  0:
    case  1:
      lastGcommand = cmd;
      G01();
      break;
    case 28:  G28();  break;
    case 90:  G90();  break;
    case 91:  G91();  break;
    default: break;
  }
}



// M COMMANDS


// M110 set line number
// valid formats include N100 M110, N200 M110 N110, and M110 N110.
void Parser::M110() {
  int firstLineNumber = -1;
  int secondLineNumber = -1;
  
  if(serialBuffer[0]=='N') {
    // line starts with a line number
    firstLineNumber = strtol(serialBuffer + 1, NULL, 10);
  }
  
  for(int i=1;i<sofar;++i) {
    if(serialBuffer[i]=='N' || serialBuffer[i]=='n') {
      secondLineNumber = strtol(serialBuffer + i + 1, NULL, 10);
    }
  }
  if(secondLineNumber!=-1) {
    lineNumber = secondLineNumber;
  } else if(firstLineNumber!=-1) {
    lineNumber = firstLineNumber;
  }

  Serial.print(F("M110 "));
  Serial.println(lineNumber);
}


// M111 set debug flags.  M111 Sn sets flags to n
void Parser::M111() {
  if (hasGCode('S')) {
    debugFlags = parseNumber('S',debugFlags);
  }
  Serial.print(F("M111 S"));
  Serial.println(debugFlags,DEC);
}


// M112 emergency stop.  Set all PIDS to zero.
void Parser::M112() {
  for (ALL_MOTORS(i)) {
    motors[i].setPID(0,0,0);
  }
}


// M114 Print the current target position (in degrees)
void Parser::M114() {
  Serial.print(F("M114"));
  for (ALL_MOTORS(i)) {
    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(motors[i].angleTarget);
  }

  //Serial.print(F(" F"));  Serial.print(feed_rate);
  //Serial.print(F(" A"));  Serial.print(acceleration);
  Serial.println();
}


/**
   M206 set home offsets
*/
void Parser::M206() {
  // cancel the current home offsets
  for (ALL_MOTORS(i)) {
    float angleHome = parseNumber( motors[i].letter, sensorManager.sensors[i].angleHomeRaw );
    sensorManager.sensors[i].angleHomeRaw = max(min(angleHome, 360), -360);
  }
}


// M306 adjust PID.
void Parser::M306() {
  if (hasGCode('L')) {
    int axis = parseNumber('L', 0);
    axis = max(min(axis, 5), 0);

    float p = parseNumber('P', motors[axis].kp );
    float i = parseNumber('I', motors[axis].ki );
    float d = parseNumber('D', motors[axis].kd );  // this only works as long as M codes are processed before D codes.

    // disable global interrupts
    CRITICAL_SECTION_START();
    motors[axis].setPID(p, i, d);
    // enable global interrupts
    CRITICAL_SECTION_END();

    // report values
    Serial.print("M306 ");
    Serial.print(motors[axis].letter);
    Serial.print(" P");
    Serial.print(p, 6);
    Serial.print(" I");
    Serial.print(i, 6);
    Serial.print(" D");
    Serial.print(d, 6);
  }
}

// M428 - set home position to the current angle values
void Parser::M428() {
  // cancel the current home offsets
  M502();

  // read the sensor
  sensorManager.update();

  // apply the new offsets
  for (ALL_SENSORS(i)) {
    sensorManager.sensors[i].angleHomeRaw = sensorManager.sensors[i].angle;
  }
  D18();
}


// M500 - save home offsets
void Parser::M500() {
  eeprom.saveHome();
}


// M501 - load home offsets
void Parser::M501() {
  eeprom.loadHome();
}


// M502 - reset the home offsets
void Parser::M502() {
#define SHP(NN)  if(NUM_SENSORS>NN) sensorManager.sensors[NN].angleHomeRaw = DH_##NN##_THETA;
  SHP(0)
  SHP(1)
  SHP(2)
  SHP(3)
  SHP(4)
  SHP(5)

  D18();
}


// M503 - report the home offsets
void Parser::M503() {
  Serial.print(F("M503"));
  for (ALL_MOTORS(i)) {
    Serial.print(' ');
    Serial.print(motors[i].letter);
    Serial.print(sensorManager.sensors[i].angleHomeRaw);
  }
  Serial.println();
}




// D COMMANDS




/**
   D17 report the 6 axis sensor values from the Sixi robot arm.
*/
void Parser::D17() {
  Serial.print(F("D17"));
  for (ALL_SENSORS(i)) {
    Serial.print('\t');
    // 360/(2^14) aka 0.02197265625deg is the minimum sensor resolution.  as such more than 3 decimal places is useless.
    Serial.print(sensorManager.sensors[i].angle, 3);
  }

#if NUM_SERVOS > 0
  Serial.print(' ');
  Serial.print((float)servos[0].read(), 2);
#endif

  Serial.print('\t');
  //Serial.print( TEST_LIMITS?'+':'-');
  Serial.print( OUT_OF_BOUNDS?'+':'-');
  Serial.println();
}


// D18 copy sensor values to motor step positions.
// get the average of a few samples.
void Parser::D18() {
  float angles[NUM_SENSORS];
  int numSamples = 10;

  for (ALL_SENSORS(i)) angles[i] = 0;

  CRITICAL_SECTION_START();

  for (int i = 0; i < numSamples; ++i) {
    sensorManager.update();
    for (ALL_SENSORS(j)) {
      angles[j] += sensorManager.sensors[j].angle;
    }
  }

  for (ALL_SENSORS(i)) {
    angles[i] /= (float)numSamples;
  }

  int32_t steps[NUM_MOTORS];
  kinematics.anglesToSteps(angles, steps);

  for (ALL_SENSORS(i)) {
    motors[i].angleTarget = angles[i];
    motors[i].stepsTarget = steps[i];
    motors[i].stepsNow[motors[i].currentPlannerStep] = steps[i];
  }

  CRITICAL_SECTION_END();
}


void Parser::D19() {
  FLIP_BIT(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_CONTINUOUS);
}


void Parser::D20() {
  SET_BIT_OFF(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_ERROR);
  ENABLE_ISR;
}


void Parser::D21() {
  int isOn = parseNumber('P',TEST_LIMITS?1:0);
  
  SET_BIT(sensorManager.positionErrorFlags,POSITION_ERROR_FLAG_CHECKLIMIT,isOn);  
  
  Serial.print(F("D21 "));
  Serial.println(TEST_LIMITS?"1":"0");
}

// D22 Save all PID to EEPROM
void Parser::D22() {
  eeprom.savePID();
}


// D50 Set strict parsing and report state.  Format D50 [Sn] where n=0 for off and 1 for true.
void Parser::D50() {
  if(hasGCode('S')) {
    int newState = parseNumber('S',IS_STRICT);
    SET_BIT(parserFlags,FLAG_STRICT,newState);
  }
  Serial.print(F("D50 "));
  Serial.println(IS_STRICT);
}



// G COMMANDS




// G0/G1 linear moves
void Parser::G01() {
  float angles[NUM_MOTORS];
  int32_t steps[NUM_MOTORS];

  // if limit testing is on and a limit is exceeeded
  if(TEST_LIMITS && OUT_OF_BOUNDS) {
    // refuse to move
    Serial.println(F("LIMIT ERROR"));
    return;
  }
      
  if(hasGCode('A')) {
    motorManager.acceleration = parseNumber('A',motorManager.acceleration);
    motorManager.acceleration = max(min(motorManager.acceleration,MAX_ACCELERATION),0.1);
  }

  uint8_t badAngles=0;
  
  //Serial.print(serialBuffer);
  //Serial.print("TO");
  for (ALL_MOTORS(i)) {
    float startP = RELATIVE_MOVES ? 0 : motors[i].angleTarget;
    float endP = motors[i].angleTarget - startP;

    angles[i] = parseNumber( motors[i].letter, startP ) + endP;

    //Serial.print(" ");
    //Serial.print(angles[i]);

    if(angles[i] > motors[i].limitMax) {
      Serial.print(F("LIMIT MAX "));
      Serial.println(i);
      badAngles=1;
    }
    if(angles[i] < motors[i].limitMin) {
      Serial.print(F("LIMIT MIN "));
      Serial.println(i);
      badAngles=1;
    }
  }
  //Serial.println();

  if(badAngles) return;
  
  float velocity[NUM_MOTORS];

  #define PARSE_GV(NN,CC)  velocity[CC] = parseNumber(NN,motors[CC].velocityTarget);
  PARSE_GV('K',0);
  PARSE_GV('P',1);
  PARSE_GV('Q',2);
  PARSE_GV('R',3);
  PARSE_GV('S',4);
  PARSE_GV('T',5);
  
  uint8_t hasVel=0;
  if(hasGCode('K')) {
    hasVel=1;
  }

  //Serial.println("G01");
  //Serial.println( RELATIVE_MOVES ? "REL" : "ABS" );

  for (ALL_MOTORS(i)) {
    /*
    Serial.println(motors[i].letter);
    //Serial.print("\tangleTarget0=");        Serial.print(motors[i].angleTarget);
    //Serial.print("\tstepsTarget0=");        Serial.print(motors[i].stepsTarget);
    Serial.print("\ta=");        Serial.print(angles[i]);
    //Serial.print("\tstepsTarget1=");        Serial.print(steps[i]);
    //Serial.print("\tstepsNow=");        Serial.print(motors[i].stepsNow);
    Serial.print("\tv=");        Serial.print(velocity[i]);
    //*/
    motors[i].angleTarget = angles[i];
    if(hasVel==1) {
      motors[i].velocityOld = motors[i].velocityTarget;
      motors[i].velocityTarget = velocity[i];
      motors[i].interpolationTime = 0;
      motors[i].totalTime = G0_SAFETY_TIMEOUT_S;
    }
  }

  kinematics.anglesToSteps(angles, steps);

  CRITICAL_SECTION_START();
  for (ALL_MOTORS(i)) {
    motors[i].stepsTarget = steps[i];
  }
  CRITICAL_SECTION_END();

#if NUM_SERVOS>0
  {
    float initial = (float)servos[0].read();
    float start  = (RELATIVE_MOVES ? 0 : initial);
    float parsed = (int32_t)floor(parseNumber( 'T', start ));
    float ending = (RELATIVE_MOVES ? initial : 0 ) + parsed;
    ending = max(min(ending, 180), 0);
    servos[0].write(floor(ending));
  }
#endif

  // in case it was disabled by limit error or e-stop
  ENABLE_ISR;
}

void Parser::G28() {
  float angles[NUM_MOTORS];
  int32_t steps[NUM_MOTORS];

  angles[0] = DH_0_THETA;
  angles[1] = DH_1_THETA;
  angles[2] = DH_2_THETA;
  angles[3] = DH_3_THETA;
  angles[4] = DH_4_THETA;
  angles[5] = DH_5_THETA;
  
  kinematics.anglesToSteps(angles, steps);

  CRITICAL_SECTION_START();
  for (ALL_MOTORS(i)) {
    motors[i].angleTarget = angles[i];
    motors[i].stepsTarget = steps[i];
  }
  CRITICAL_SECTION_END();
}

void Parser::G90() {
  SET_BIT_ON(parserFlags, FLAG_RELATIVE);
}


void Parser::G91() {
  SET_BIT_OFF(parserFlags, FLAG_RELATIVE);
}
