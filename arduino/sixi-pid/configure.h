#pragma once

//#define UNIT1  // uncomment for the very first Sixi only

// wrong board type set
#ifndef __AVR_ATmega2560__
  #error "Oops!  Make sure you have 'Arduino Mega 2560' selected from the 'Tools -> Boards' menu."
#endif


#define MACHINE_STYLE_NAME           "SIXI"
#define MACHINE_HARDWARE_VERSION     6  // yellow sixi 2019


#include <SPI.h>  // pkm fix for Arduino 1.5
#include <Arduino.h>  // for type definitions
#include <EEPROM.h>
#include <stdint.h>


// for assembly in isr inner loop
#define A(CODE) " " CODE "\n\t"

// optimize code, please
#define FORCE_INLINE         __attribute__((always_inline)) inline

// convenience
#define PENDING(NOW,SOON) ((uint32_t)(NOW-(SOON))<0)
#define ELAPSED(NOW,SOON) (!PENDING(NOW,SOON))

#define TEST(FF,NN)       ((FF & NN) == NN )

// serial comms
#define BAUD 57600
#define MAX_BUF 127


FORCE_INLINE float capRotationDegrees(double arg0,double centerPoint) {
  while(arg0<centerPoint-180) arg0 += 360;
  while(arg0>centerPoint+180) arg0 -= 360;
  
  return arg0;
}


#include "eeprom.h"
#include "motors.h"
#include "sensors.h"
#include "clock.h"
#include "parser.h"
