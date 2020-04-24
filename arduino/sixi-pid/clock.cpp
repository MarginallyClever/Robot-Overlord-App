//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t _sreg=0;

static float STEP_PER_SECOND = (float)CLOCK_MAX_STEP_FREQUENCY;

uint8_t isr_step_multiplier = 1;


ISR(TIMER1_COMPA_vect) {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  CRITICAL_SECTION_START();
  
  // (AVR enters the ISR with global interrupts disabled, so no need to do it here)
  //for( int j=0; j<isr_step_multiplier;++j ) {
    for(ALL_MOTORS(i)) {
      motors[i].update(STEP_PER_SECOND,sensorAngles[i]);
    }
  //}

  // Turn the interrupts back on (reduces UART delay, apparently)
  CRITICAL_SECTION_END();
}
