//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"


uint8_t _sreg=0;

uint16_t usPerTickISR = 1;
float sPerTickISR = 0.000001;

uint8_t stepMultiplierISR = 1;


void clockSetup() {
  // disable global interrupts
  CRITICAL_SECTION_START();
    // using TIMER1 on the mega
    
    // clear registers
    TCCR1A = 0;  // normal counting mode
    // turn on CTC mode
    TCCR1B = (1 << WGM12);
    // Set 8x prescaler
    TCCR1B = (TCCR1B & ~(0x07 << CS10)) | (2 << CS10);
    TCNT1 = 0;  // overflow clock that triggers the interrupt
    // set compare match register to desired timer count
    OCR1A = 2000;  // with 8x prescaler this is 1ms, because ((16000000 / 8) / 2000) = 1000ms.
    SBI(TIFR1, OCF1A);     // clear any pending interrupts;
    SBI(TIMSK1, OCIE1A);   // enable the output compare interrupt
    
    stepMultiplierISR=1;
    uint32_t interval = 64;
    uint32_t callsPerSecond = (TIMER_RATE / interval);
    usPerTickISR = 1000000.0f/(float)(callsPerSecond*stepMultiplierISR);
    sPerTickISR = usPerTickISR*0.000001;

    CLOCK_ADJUST(interval);
    
    if(false) {
      Serial.print(F("Hz="));              Serial.println(CLOCK_MAX_ISR_FREQUENCY);
      Serial.print(F("interval="));        Serial.println(interval);
      Serial.print(F("multiplier="));      Serial.println(stepMultiplierISR);
      Serial.print(F("callsPerSecond="));  Serial.println(callsPerSecond);
      Serial.print(F("usPerTickISR="));    Serial.println(usPerTickISR,6);
    }
  
    // enable timer compare interrupt
    TIMSK1 |= (1 << OCIE1A);
    
  // enable global interrupts
  CRITICAL_SECTION_END();
}

FORCE_INLINE void ISRInternal() {
  // AVR enters the ISR with global interrupts disabled, so no need to do it here
  for( int j=0; j<stepMultiplierISR; ++j ) {
    for(ALL_MOTORS(i)) {
      motors[i].ISRStepNow();
    }
  }
}


ISR(TIMER1_COMPA_vect) {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  CRITICAL_SECTION_START();
  uint16_t oldTime = OCR1A;
  CLOCK_ADJUST(MAX_OCR1A_VALUE);
  // Turn the interrupts back on (reduces UART delay, apparently)
  CRITICAL_SECTION_END();

  ISRInternal();

  // do we need to adjust the clock timer?
  uint32_t smallestStepInterval = 0xFFFFFFFF;  // max
  for(ALL_MOTORS(i)) {
    uint8_t index = motors[i].currentPlannerStep;
    uint32_t s = motors[i].stepInterval_us[index];
    if( smallestStepInterval > s && s!=0 ) {
      smallestStepInterval = s;
    }
  }
  uint32_t hz = 1000000.0 / (float)smallestStepInterval;  // frequency = (one second) / (smallest number of steps)
  hz = min(max(hz,1),500000);
  uint16_t newTime = calc_timer(hz,&stepMultiplierISR);

  // set the new time.
  CRITICAL_SECTION_START();
  CLOCK_ADJUST(newTime);
  CRITICAL_SECTION_END();
}


void clockISRProfile() {
  // Disable interrupts, to avoid ISR preemption while we reprogram the period
  //CRITICAL_SECTION_START();
  // make sure the stepMultiplierISR is 1
  int oldMult = stepMultiplierISR;
  stepMultiplierISR=1;

  int count=1000;
  // get set... go!
  uint32_t tStart = micros();
  
  for(int i=0;i<count;++i) {
    ISRInternal();
  }
  
  uint32_t tEnd = micros();
  
  // restore stepMultiplierISR
  stepMultiplierISR=oldMult;
  
  // Turn the interrupts back on (reduces UART delay, apparently)
  //CRITICAL_SECTION_END();
  
  // report results
  uint32_t dt = tEnd-tStart;
  float dtPer = (float)dt / (float)count;
  Serial.print(F("profile loops     ="));  Serial.println(count);
  Serial.print(F("profile total time="));  Serial.println(dt);
  Serial.print(F("profile per loop  ="));  Serial.println(dtPer);
}
