#pragma once



// state flags for the parser
#define FLAG_RELATIVE      (0)  // relative moves
#define FLAG_STRICT        (1)  // force CRC check on all commands

#define RELATIVE_MOVES     (TEST(parserFlags,FLAG_RELATIVE))
#define IS_STRICT          (TEST(parserFlags,FLAG_STRICT))

class Parser {
public:
  char serialBuffer[MAX_BUF + 1]; // Serial buffer
  int sofar;                      // Serial buffer progress
  uint32_t lastCmdTimeMs;         // prevent timeouts
  int32_t lineNumber = 0;        // make sure commands arrive in order
  uint8_t lastGcommand = -1;
  
  uint8_t parserFlags = 0;

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

  void M110();  // set and report line number.  M110 [Nn] sets next expected line number to n.
  void M111();  // set and report debug level.  M111 [Sn] sets flags to n.  Combine any valid flags: 1 (echo)
  void M112();  // M112 emergency stop.  Set all PIDS to zero.
  void M114();  // report current target position
  void M206();  // set home offsets M206 [Xn] [Yn] [Zn] [Un] [Vn] [Wn]
  void M306();  // adjust PID  M306 L[0...5] [Pn] [In] [Dn] and report new values.
  void M428();  // set home position to the current raw angle values (don't use home position to adjust home position!)
  void M500();  // save home offsets
  void M501();  // load home offsets
  void M502();  // reset the home offsets
  void M503();  // report the home offsets
  
  void D17();   // report sensor values
  void D18();   // copy sensor values to motor step positions
  //void D19();   // toggle continuous D17 reporting
  //void D20();
  //void D21();  
  void D22();   // Save all PID values to EEPROM
  
  void D50();   // Set and report strict mode.  D50 Sn where n=0 for off and 1 for on.
  
  void G01();  // G0/G1 [Xn] [Yn] [Zn] [Un] [Vn] [Wn]
  void G28();  // go home
  void G90();  // set absolute mode
  void G91();  // set relative mode
};

extern Parser parser;
