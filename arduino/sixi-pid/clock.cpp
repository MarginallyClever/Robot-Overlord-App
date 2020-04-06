//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t _sreg=0;

uint32_t current_feed_rate = 500;

uint8_t isr_step_multiplier = 1;


ISR(TIMER1_COMPA_vect) {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  CRITICAL_SECTION_START();
  
  // (AVR enters the ISR with global interrupts disabled, so no need to do it here)
  for( int j=0; j<isr_step_multiplier;++j ) {
    for( int i=0; i<NUM_MOTORS; ++i ) {
      motors[i].update((float)current_feed_rate/1000000.0);
    }
  }

  // Turn the interrupts back on (reduces UART delay, apparently)
  CRITICAL_SECTION_END();
}
