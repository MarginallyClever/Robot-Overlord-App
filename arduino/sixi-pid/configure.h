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

#ifndef SBI
#define SBI(NN,BB)          (NN |=  (1<<(BB)))
#endif
#ifndef CBI
#define CBI(NN,BB)          (NN &= ~(1<<(BB)))
#endif

#define TEST(NN,BB)         (NN & (1<<BB) == (1<<BB))
#define SET_BIT(NN,BB,TF)   do { if(TF) SBI(NN,BB); else CBI(NN,BB); } while(0);
#define SET_BIT_ON(NN,BB)   SBI(NN,BB);
#define SET_BIT_OFF(NN,BB)  CBI(NN,BB);

// serial comms
#define BAUD    250000 // was 57600
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
#include "dh_parameters.h"


// Sanity checks
#if NUM_SENSORS != NUM_MOTORS
  #error "Oops!  NUM_SENSORS != NUM_MOTORS"
#endif
