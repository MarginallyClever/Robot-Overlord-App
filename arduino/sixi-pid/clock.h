#pragma once

#include "speed_lookuptable.h"

// CLOCK 

// for timer interrupt control
#undef F_CPU
#define F_CPU                   (16000000L)

#define MAX_COUNTER             (65536L)  // 16 bits

#define TIMER_RATE              ((F_CPU)/8)  // 8 is for 8x prescale multiplier

// 1.8deg stepper, 1/1 microstepping -> 50 deg/s = ~27.7 steps/s

#define CLOCK_MAX_STEP_FREQUENCY (3000L)// was 240000L
#define CLOCK_MIN_STEP_FREQUENCY (F_CPU/500000U)

#define TIMEOUT_OK (1000)

#ifndef MIN_SEGMENT_TIME_US
#define MIN_SEGMENT_TIME_US  (1000000.0/CLOCK_MAX_STEP_FREQUENCY)  // actual minimum on mega? 5000.
#endif

#ifndef MAX_OCR1A_VALUE
#define MAX_OCR1A_VALUE (0xFFFF)
#endif

#define CLOCK_ADJUST(x)  {  OCR1A = (x);  }  // microseconds



extern uint8_t _sreg;
extern uint8_t isr_step_multiplier;


FORCE_INLINE void CRITICAL_SECTION_START() {
  _sreg = SREG;  cli();
}

FORCE_INLINE void CRITICAL_SECTION_END() {
  SREG = _sreg;
}


// intRes = intIn1 * intIn2 >> 16
// uses:
// r26 to store 0
// r27 to store the byte 1 of the 24 bit result
FORCE_INLINE uint16_t MultiU16X8toH16(uint8_t charIn1, uint16_t intIn2) {
  register uint8_t tmp;
  register uint16_t intRes;
  __asm__ __volatile__ (
    A("clr %[tmp]")
    A("mul %[charIn1], %B[intIn2]")
    A("movw %A[intRes], r0")
    A("mul %[charIn1], %A[intIn2]")
    A("add %A[intRes], r1")
    A("adc %B[intRes], %[tmp]")
    A("lsr r0")
    A("adc %A[intRes], %[tmp]")
    A("adc %B[intRes], %[tmp]")
    A("clr r1")
      : [intRes] "=&r" (intRes),
        [tmp] "=&r" (tmp)
      : [charIn1] "d" (charIn1),
        [intIn2] "d" (intIn2)
      : "cc"
  );
  return intRes;
}


// intRes = longIn1 * longIn2 >> 24
// uses:
// A[tmp] to store 0
// B[tmp] to store bits 16-23 of the 48bit result. The top bit is used to round the two byte result.
// note that the lower two bytes and the upper byte of the 48bit result are not calculated.
// this can cause the result to be out by one as the lower bytes may cause carries into the upper ones.
// B A are bits 24-39 and are the returned value
// C B A is longIn1
// D C B A is longIn2
//
FORCE_INLINE uint16_t MultiU24X32toH16(uint32_t longIn1, uint32_t longIn2) {
#ifdef ESP8266
  uint16_t intRes = longIn1 * longIn2 >> 24;
#else // ESP8266
  register uint8_t tmp1;
  register uint8_t tmp2;
  register uint16_t intRes;
  __asm__ __volatile__(
    A("clr %[tmp1]")
    A("mul %A[longIn1], %B[longIn2]")
    A("mov %[tmp2], r1")
    A("mul %B[longIn1], %C[longIn2]")
    A("movw %A[intRes], r0")
    A("mul %C[longIn1], %C[longIn2]")
    A("add %B[intRes], r0")
    A("mul %C[longIn1], %B[longIn2]")
    A("add %A[intRes], r0")
    A("adc %B[intRes], r1")
    A("mul %A[longIn1], %C[longIn2]")
    A("add %[tmp2], r0")
    A("adc %A[intRes], r1")
    A("adc %B[intRes], %[tmp1]")
    A("mul %B[longIn1], %B[longIn2]")
    A("add %[tmp2], r0")
    A("adc %A[intRes], r1")
    A("adc %B[intRes], %[tmp1]")
    A("mul %C[longIn1], %A[longIn2]")
    A("add %[tmp2], r0")
    A("adc %A[intRes], r1")
    A("adc %B[intRes], %[tmp1]")
    A("mul %B[longIn1], %A[longIn2]")
    A("add %[tmp2], r1")
    A("adc %A[intRes], %[tmp1]")
    A("adc %B[intRes], %[tmp1]")
    A("lsr %[tmp2]")
    A("adc %A[intRes], %[tmp1]")
    A("adc %B[intRes], %[tmp1]")
    A("mul %D[longIn2], %A[longIn1]")
    A("add %A[intRes], r0")
    A("adc %B[intRes], r1")
    A("mul %D[longIn2], %B[longIn1]")
    A("add %B[intRes], r0")
    A("clr r1")
    : [intRes] "=&r" (intRes),
    [tmp1] "=&r" (tmp1),
    [tmp2] "=&r" (tmp2)
    : [longIn1] "d" (longIn1),
    [longIn2] "d" (longIn2)
    : "cc"
  );
#endif // ESP8266
  return intRes;
}


/**
   Set the clock 2 timer frequency.
   @input desired_freq_hz the desired frequency
   Different clock sources can be selected for each timer independently.
   To calculate the timer frequency (for example 2Hz using timer1) you will need:
*/
static FORCE_INLINE unsigned short calc_timer(uint32_t desired_freq_hz, uint8_t*loops) {
  uint32_t timer;
  uint8_t step_multiplier = 1;

  int idx=0;
  while( idx<7 && desired_freq_hz > 10000 ) {
    step_multiplier <<= 1;
    desired_freq_hz >>= 1;
    idx++;
  }
  *loops = step_multiplier;
  
  if( desired_freq_hz < CLOCK_MIN_STEP_FREQUENCY ) desired_freq_hz = CLOCK_MIN_STEP_FREQUENCY;
  desired_freq_hz -= CLOCK_MIN_STEP_FREQUENCY;
  if(desired_freq_hz >= 8*256) {
    const uint8_t tmp_step_rate = (desired_freq_hz & 0x00FF);
    const uint16_t table_address = (uint16_t)&speed_lookuptable_fast[(uint8_t)(desired_freq_hz >> 8)][0],
                   gain = (uint16_t)pgm_read_word_near(table_address + 2);
    timer = MultiU16X8toH16(tmp_step_rate, gain);
    timer = (uint16_t)pgm_read_word_near(table_address) - timer;
  } else { // lower step rates
    uint16_t table_address = (uint16_t)&speed_lookuptable_slow[0][0];
    table_address += ((desired_freq_hz) >> 1) & 0xFFFC;
    timer = (uint16_t)pgm_read_word_near(table_address)
          - (((uint16_t)pgm_read_word_near(table_address + 2) * (uint8_t)(desired_freq_hz & 0x0007)) >> 3);
  }
  
  return timer;
}

extern void clockSetup();
extern void clockISRProfile();
