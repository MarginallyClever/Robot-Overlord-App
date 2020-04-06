#pragma once

extern uint32_t lastCmdTimeMs;    // prevent timeouts
extern uint32_t reportDelay;  // how long since last D17 sent out

class Parser {
public:
  static uint8_t hasGCode(char code);
  static float parseNumber(char code, float val);
  static char checkLineNumberAndCRCisOK();

  static void M114();
  static void M206();
  static void M306();
  static void M428();
  static void M500();
  static void M501();
  static void M502();
  static void M503();
  
  static void D17();
  static void D18();
  static void D19();
  
  static void G01();
  
  static void ready();
  
  static void update();
  
  static void processCommand();
};

extern Parser parser;
