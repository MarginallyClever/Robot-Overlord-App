#pragma once


// state flags for the parser
#define FLAG_RELATIVE          (0)  // relative moves
#define FLAG_STRICT            (1)  // force CRC check on all commands

#define RELATIVE_MOVES         (TEST(parserFlags,FLAG_RELATIVE))
#define IS_STRICT              (TEST(parserFlags,FLAG_STRICT))

#define G0_SAFETY_TIMEOUT_S    (1.0/30.0)  // 30 fps
#define G0_SAFETY_TIMEOUT_MS   (G0_SAFETY_TIMEOUT_S*1000)


class Parser {
public:
  char serialBuffer[MAX_BUF + 1]; // Serial buffer
  int sofar;                      // Serial buffer progress
  uint32_t lastCmdTimeMs;         // prevent timeouts
  int32_t lineNumber = 0;        // make sure commands arrive in order
  uint8_t lastGcommand = -1;
  
  uint8_t parserFlags = 0;

  void setup();
  
  // does this command have the matching code?
  uint8_t hasGCode(char code);
  // find the matching code and return the number that immediately follows it.
  float parseNumber(char code, float val);
  // if there is a line number, checks it is the correct line number.  if there is a * at the end, checks the string is valid.
  char checkLineNumberAndCRCisOK();
  // signal through serial I am ready to receive more commands
  void ready();
  // read any available serial data
  void update();
  // called by update when an entire command is received.
  void processCommand();

  // COMMANDS
  // w means whole number.  b means binary (0/1).  d means decimal number. [] means optional.
  // all commands must end with the new line (\n, hex=0xD, dec=13) character.
  
  void M110();  // M110 [Nw] - Set and report line number.  If no number given, report only.
  void M111();  // M111 [Sw] - Ssets flags to n.  If no number given, report only.  Combine any valid flags: 1 (echo)
  void M112();  // M112 - Emergency stop.  Set all PIDS to zero.
  void M114();  // - Report current target position, max feedrate, and max acceleration.
  void M206();  // M206 [Xd] [Yd] [Zd] [Ud] [Vd] [Wd] - Set and report home offsets.  If no number given, report only.
  void M306();  // M306 L(0...5) [Pd] [Id] [Dd] - Adjust PID and report new values.  If no number given, report only.  L is the joint number where L0 is X L5 is W.  
  void M428();  // - Set home position to the current raw angle values.  As soon as this executes the robot will believe the joint angles are X0 Y-90 Z0 U0 V0 W0.
  void M500();  // - Save home offsets.
  void M501();  // - Load home offsets.
  void M502();  // - Reset the home offsets to zero.
  void M503();  // - Report the home offsets.
  
  void D17();  // - Report adjusted sensor values.
  void D18();  // - Copy sensor values to motor step positions.  Deprecated?
  void D19();  // - Toggle continuous D17 reporting.
  void D20();  // - Turn off position error.
  void D21();  // D21 [Pb] - Set and report limit checking.
  void D22();  // - Save all PID values to EEPROM.
  void D50();  // D50 [Sb] - Set and report strict mode.  When strict mode is on all commands MUST have a checksum.
  
  void G01();  // G0/G1 [Xd] [Yd] [Zd] [Ud] [Vd] [Wd] - Set the target adjusted joint angles to the values provided.  If a value is not given, the target angle for that joint does not change.  If any part of the requested destination is outside of range a "LIMIT MIN/MAX [joint number]" for each part will be displayed and no action will be taken.
  void G28();  // - Go to home position
  void G90();  // - Set absolute mode
  void G91();  // - Set relative mode
};

extern Parser parser;
