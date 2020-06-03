#pragma once

#include <arduino.h>

// for assembly in isr inner loop
#define A(CODE) " " CODE "\n\t"

// optimize code, please
#define FORCE_INLINE         __attribute__((always_inline)) inline

// convenience
#define PENDING(NOW,SOON) ((uint32_t)(NOW-(SOON))<0)
#define ELAPSED(NOW,SOON) (!PENDING(NOW,SOON))

#define BIT_FOR_FLAG(FF)    (1<<FF)

#ifndef SBI
#define SBI(NN,BB)          (NN |=  (1<<BB))
#endif
#ifndef CBI
#define CBI(NN,BB)          (NN &= ~(1<<BB))
#endif

#define TEST(NN,BB)         ((NN & BIT_FOR_FLAG(BB)) == BIT_FOR_FLAG(BB))
#define SET_BIT_ON(NN,BB)   SBI(NN,BB)
#define SET_BIT_OFF(NN,BB)  CBI(NN,BB)
#define SET_BIT(NN,BB,TF)   do { if(TF) SET_BIT_ON(NN,BB); else SET_BIT_OFF(NN,BB); } while(0);
#define FLIP_BIT(NN,BB)     (NN ^= BIT_FOR_FLAG(BB))


// wrap all degrees to within -180...180.
FORCE_INLINE float WRAP_DEGREES(float n) {
  n = fmod(n,360);
  n +=360;
  n = fmod(n,360);
  if(n>180) n-=360;
  return n;
}

// wrap all radians within -PI...PI
FORCE_INLINE float WRAP_RADIANS(float n) {
  n = fmod(n,TWO_PI);
  n += TWO_PI;
  n = fmod(n,TWO_PI);
  if(n>PI) n-=TWO_PI;
  return n;
}

// wrap n within center-range/2...center+range/2
FORCE_INLINE int32_t WRAP_LONG(int32_t n,int32_t range,int32_t center) {
  n = n%range;
  n += range;
  n = n%range;
  if(n>center) n-=range;
  return n;
}

// use in for(ALL_AXIES(i)) { //i will be rising
#define ALL_AXIES(NN)  int NN=0;NN<NUM_AXIES;++NN

// use in for(ALL_MOTORS(i)) { //i will be rising
#define ALL_MOTORS(NN) int NN=0;NN<NUM_MOTORS;++NN

#define MACRO6(AA)  AA(0) AA(1) AA(2) AA(3) AA(4) AA(5)
