#pragma once



// state flags for the parser
#define FLAG_RELATIVE      (0)
#define RELATIVE_MOVES     (TEST(motionFlags,FLAG_RELATIVE))


class Parser {
public:
  char serialBuffer[MAX_BUF + 1]; // Serial buffer
  int sofar;                      // Serial buffer progress
  uint32_t lastCmdTimeMs;         // prevent timeouts
  int32_t lineNumber = 0;        // make sure commands arrive in order
  uint8_t lastGcommand = -1;
  
  uint16_t motionFlags = 0;


  uint8_t hasGCode(char code);
  float parseNumber(char code, float val);
  char checkLineNumberAndCRCisOK();

  void M114();  // report current target position
  void M206();  // set home offsets M206 [Xn] [Yn] [Zn] [Un] [Vn] [Wn]
  void M306();  // adjust PID  M306 L[0...5] [Pn] [In] [Dn]
  void M428();  // set home position to the current raw angle values (don't use home position to adjust home position!)
  void M500();  // save home offsets
  void M501();  // load home offsets
  void M502();  // reset the home offsets
  void M503();  // report the home offsets
  
  void D17();   // report sensor values
  void D18();   // copy sensor values to motor step positions
  //void D19();   // toggle continuous D17 reporting
  
  void G01();  // G0/G1 [Xn] [Yn] [Zn] [Un] [Vn] [Wn]
  void G90();  // set absolute mode
  void G91();  // set relative mode
  
  void ready();
  
  void update();
  
  void processCommand();
};

extern Parser parser;
