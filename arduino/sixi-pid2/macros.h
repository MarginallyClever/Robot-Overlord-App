#pragma once


// for assembly in isr inner loop
#define A(CODE) " " CODE "\n\t"

// optimize code, please
#define FORCE_INLINE         __attribute__((always_inline)) inline

// convenience
#define PENDING(NOW,SOON) ((uint32_t)(NOW-(SOON))<0)
#define ELAPSED(NOW,SOON) (!PENDING(NOW,SOON))

#ifndef SBI
#define SBI(NN,BB)          (NN |=  (1<<BB))
#endif
#ifndef CBI
#define CBI(NN,BB)          (NN &= ~(1<<BB))
#endif

#define TEST(NN,BB)         ((NN & (1<<BB)) == (1<<BB))
#define SET_BIT_ON(NN,BB)   SBI(NN,BB)
#define SET_BIT_OFF(NN,BB)  CBI(NN,BB)
#define SET_BIT(NN,BB,TF)   do { if(TF) SBI(NN,BB); else CBI(NN,BB); } while(0);
#define FLIP_BIT(NN,BB)     (NN ^= (1<<BB))


// wrap all degrees to within -180...180.
#define WRAP_DEGREES(NN)     (fmod( (NN+180), 360 ) - 180)
// wrapp all radians within -PI...PI
#define WRAP_RADIANS(NN)     (fmod( (NN+PI), PI*2 ) - PI)
