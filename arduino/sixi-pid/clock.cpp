//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t _sreg=0;

float SECONDS_PER_ISR_TICK = 1;

uint8_t isr_step_multiplier = 1;


void clockSetup() {
  // disable global interrupts
  CRITICAL_SECTION_START();
  
    // set entire TCCR1A register to 0
    TCCR1A = 0;
    // set the overflow clock to 0
    TCNT1  = 0;
    // set compare match register to desired timer count
    OCR1A = 2000;  // with 8x prescaler this is 1s.
    // turn on CTC mode
    TCCR1B = (1 << WGM12);
    // Set 8x prescaler
    TCCR1B = (TCCR1B & ~(0x07 << CS10)) | (2 << CS10);
    
    uint32_t interval = calc_timer(CLOCK_MAX_STEP_FREQUENCY, &isr_step_multiplier);
    SECONDS_PER_ISR_TICK = 1.0 / (float)(CLOCK_MAX_STEP_FREQUENCY * isr_step_multiplier);

    Serial.print(F("Hz="));    Serial.println(CLOCK_MAX_STEP_FREQUENCY);
    Serial.print(F("interval="));    Serial.println(interval);
    Serial.print(F("multiplier="));  Serial.println(isr_step_multiplier);
    Serial.print(F("s/tick="));  Serial.println(SECONDS_PER_ISR_TICK,6);
    
    CLOCK_ADJUST(interval);
    // enable timer compare interrupt
    TIMSK1 |= (1 << OCIE1A);
    
  // enable global interrupts
  CRITICAL_SECTION_END();
}


FORCE_INLINE void ISRInternal() {
  // (AVR enters the ISR with global interrupts disabled, so no need to do it here)
  for( int j=0; j<isr_step_multiplier;++j ) {
    for(ALL_MOTORS(i)) {
      motors[i].update(SECONDS_PER_ISR_TICK,sensorAngles[i]);
    }
  }
}


ISR(TIMER1_COMPA_vect) {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  //CRITICAL_SECTION_START();

  ISRInternal();

  // Turn the interrupts back on (reduces UART delay, apparently)
  //CRITICAL_SECTION_END();
}

void clockISRProfile() {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  CRITICAL_SECTION_START();
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
  CRITICAL_SECTION_END();
  
  // report results
  uint32_t dt = tEnd-tStart;
  float dtPer = (float)dt / (float)count;
  Serial.print(F("profile loops     ="));  Serial.println(count);
  Serial.print(F("profile total time="));  Serial.println(dt);
  Serial.print(F("profile per loop  ="));  Serial.println(dtPer);
}
