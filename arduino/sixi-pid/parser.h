#pragma once

extern uint32_t lastCmdTimeMs;    // prevent timeouts
extern uint32_t reportDelay;  // how long since last D17 sent out

class Parser {
public:
  static uint8_t hasGCode(char code);
  static float parseNumber(char code, float val);
  static char checkLineNumberAndCRCisOK();

  static void M114();  // report current angle values
  static void M206();  // set home offsets M206 Xn Yn Zn Un Vn Wn
  static void M306();  // adjust PID  M306 L[0...5] [Pn] [In] [Dn]
  static void M428();  // set home position to the current angle values
  static void M500();  // save home offsets
  static void M501();  // load home offsets
  static void M502();  // reset the home offsets
  static void M503();  // report the home offsets
  
  static void D17();   // report sensor values
  static void D18();   // copy sensor values to motor step positions
  //static void D19();   // toggle continuous D17 reporting
  
  static void G01();
  static void G90();
  static void G91();
  
  static void ready();
  
  static void update();
  
  static void processCommand();
};

extern Parser parser;
