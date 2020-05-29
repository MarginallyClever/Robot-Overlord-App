#pragma once

//#define UNIT1  // uncomment for the very first Sixi only

// wrong board type set
#ifndef __AVR_ATmega2560__
  #error "Oops!  Make sure you have 'Arduino Mega 2560' selected from the 'Tools -> Boards' menu."
#endif

#define MACHINE_STYLE_NAME           "SIXI"
#define MACHINE_HARDWARE_VERSION     6  // yellow sixi 2019

#define BOARD_MEGA2560               0
#define BOARD_RUMBA                  1

#define MOTHERBOARD                  BOARD_MEGA2560
//#define MOTHERBOARD                  BOARD_RUMBA  // change this


// serial comms
#define BAUD                         57600
#define MAX_BUF                      150


//#include <SPI.h>  // pkm fix for Arduino 1.5
#include <Arduino.h>  // for type definitions
#include <EEPROM.h>
#include <stdint.h>


// debug flags
#define FLAG_ECHO          (1)  // echo all commands received
#define FLAG_INFO          (2)  // extended info
#define FLAG_ERRORS        (4)  // extra error info
#define FLAG_DRYRUN        (8)  // don't move, don't save, just report results.
#define MUST_ECHO          (TEST(debugFlags,FLAG_ECHO))
#define IS_DRYRUN          (TEST(debugFlags,FLAG_DRYRUN))
extern uint8_t debugFlags;


#include "pins_rumba.h"
#include "pins_mega2560.h"

#include "macros.h"

FORCE_INLINE float capRotationDegrees(double arg0,double centerPoint) {
  while(arg0<centerPoint-180) arg0 += 360;
  while(arg0>centerPoint+180) arg0 -= 360;
  
  return arg0;
}

#include "motors.h"
#include "sensors.h"
#include "eeprom.h"
#include "clock.h"
#include "kinematics.h"
#include "parser.h"
