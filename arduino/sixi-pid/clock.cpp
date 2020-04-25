//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t _sreg=0;

float MS_PER_ISR_TICK = 1;

uint8_t isr_step_multiplier = 1;


void clockSetup() {
  // disable global interrupts
  CRITICAL_SECTION_START();
    // using TIMER1 on the mega
    
    // clear registers
    TCCR1A = 0;  // normal counting mode
    TCCR1B = _BV(CS11);     // set prescaler of 8
    TCNT1 = 0;  // overflow clock that triggers the interrupt
    // set compare match register to desired timer count
    OCR1A = 2000;  // with 8x prescaler this is 1ms, because ((16000000 / 8) / 2000) = 1000ms.
    SBI(TIFR1, OCF1A);     // clear any pending interrupts;
    SBI(TIMSK1, OCIE1A);   // enable the output compare interrupt
    
    uint32_t interval = calc_timer(CLOCK_MAX_ISR_FREQUENCY, &isr_step_multiplier);
    uint32_t callsPerSecond = (TIMER_RATE / interval);
    MS_PER_ISR_TICK = 1000.0f/(float)callsPerSecond;

    CLOCK_ADJUST(interval);
    
    if(true) {
      Serial.print(F("Hz="));              Serial.println(CLOCK_MAX_ISR_FREQUENCY);
      Serial.print(F("interval="));        Serial.println(interval);
      Serial.print(F("multiplier="));      Serial.println(isr_step_multiplier);
      Serial.print(F("callsPerSecond="));  Serial.println(callsPerSecond);
      Serial.print(F("ms/tick="));          Serial.println(MS_PER_ISR_TICK,6);
    }
  
    // enable timer compare interrupt
    TIMSK1 |= (1 << OCIE1A);
    
  // enable global interrupts
  CRITICAL_SECTION_END();

}


FORCE_INLINE void ISRInternal() {
  // (AVR enters the ISR with global interrupts disabled, so no need to do it here)
  for( int j=0; j<isr_step_multiplier;++j ) {
    for(ALL_MOTORS(i)) {
      motors[i].update(MS_PER_ISR_TICK,sensorAngles[i]);
    }
  }
}


ISR(TIMER1_COMPA_vect) {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  CRITICAL_SECTION_START();
  uint32_t oldTime = OCR1A;
  CLOCK_ADJUST(MAX_OCR1A_VALUE);
  // Turn the interrupts back on (reduces UART delay, apparently)
  CRITICAL_SECTION_END();

  ISRInternal();

  // return the ISR where it used to be.
  CRITICAL_SECTION_START();
  CLOCK_ADJUST(oldTime);
  CRITICAL_SECTION_END();
}


void clockISRProfile() {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  //CRITICAL_SECTION_START();
  // make sure the isr_step_multiplier is 1
  int oldMult = isr_step_multiplier;
  isr_step_multiplier=1;

  int i, count=1000;
  // get set... go!
  uint32_t tStart = micros();
  
  for(int i=0;i<count;++i) {
    ISRInternal();
  }
  
  uint32_t tEnd = micros();
  
  // restore isr_step_multiplier
  isr_step_multiplier=oldMult;
  
  // Turn the interrupts back on (reduces UART delay, apparently)
  //CRITICAL_SECTION_END();
  
  // report results
  uint32_t dt = tEnd-tStart;
  float dtPer = (float)dt / (float)count;
  Serial.print(F("profile loops     ="));  Serial.println(count);
  Serial.print(F("profile total time="));  Serial.println(dt);
  Serial.print(F("profile per loop  ="));  Serial.println(dtPer);
}
