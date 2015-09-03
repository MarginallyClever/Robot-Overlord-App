//------------------------------------------------------------------------------
// Arm3 - Three Axis Robot Arm based on code from 6 Axis CNC Demo v2
// dan@marginallycelver.com 2014-03-23
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.marginallyclever.com/ for more information.

//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
#include "configure.h"

//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------
/**
 * Supports movement with both styles of Motor Shield
 * @input newx the destination x position
 * @input newy the destination y position
 **/
void motor_onestep(int motor_id,int dir) {
#if VERBOSE > 2
  Serial.print(motor_names[motor_id]);
#endif
  Motor &a = motors[motor_id];

  digitalWrite(a.dir_pin,dir*a.flip>0?LOW:HIGH);
  digitalWrite(a.step_pin,HIGH);
  digitalWrite(a.step_pin,LOW);
}


void motor_setup() {
  // set up the pins
  motors[0].step_pin=MOTOR_0_STEP_PIN;
  motors[0].dir_pin=MOTOR_0_DIR_PIN;
  motors[0].enable_pin=MOTOR_0_ENABLE_PIN;
  motors[0].limit_switch_pin=MOTOR_0_LIMIT_PIN;
  motors[0].flip=-1;

  motors[1].step_pin=MOTOR_1_STEP_PIN;
  motors[1].dir_pin=MOTOR_1_DIR_PIN;
  motors[1].enable_pin=MOTOR_1_ENABLE_PIN;
  motors[1].limit_switch_pin=MOTOR_1_LIMIT_PIN;
  motors[1].flip=-1;

  motors[2].step_pin=MOTOR_2_STEP_PIN;
  motors[2].dir_pin=MOTOR_2_DIR_PIN;
  motors[2].enable_pin=MOTOR_2_ENABLE_PIN;
  motors[2].limit_switch_pin=MOTOR_2_LIMIT_PIN;
  motors[2].flip=1;

#if NUM_AXIES > 3
  motors[3].step_pin=MOTOR_3_STEP_PIN;
  motors[3].dir_pin=MOTOR_3_DIR_PIN;
  motors[3].enable_pin=MOTOR_3_ENABLE_PIN;
  motors[3].limit_switch_pin=MOTOR_3_LIMIT_PIN;
  motors[3].flip=-1;
#endif
#if NUM_AXIES > 4
  motors[4].step_pin=MOTOR_4_STEP_PIN;
  motors[4].dir_pin=MOTOR_4_DIR_PIN;
  motors[4].enable_pin=MOTOR_4_ENABLE_PIN;
  motors[4].limit_switch_pin=MOTOR_4_LIMIT_PIN;
  motors[4].flip=1;
#endif
#if NUM_AXIES > 5
  motors[5].step_pin=MOTOR_5_STEP_PIN;
  motors[5].dir_pin=MOTOR_5_DIR_PIN;
  motors[5].enable_pin=MOTOR_5_ENABLE_PIN;
  motors[5].limit_switch_pin=MOTOR_5_LIMIT_PIN;
  motors[5].flip=1;
#endif
  
  for(int i=0;i<NUM_AXIES;++i) {  
    // set the motor pin & scale
    pinMode(motors[i].step_pin,OUTPUT);
    pinMode(motors[i].dir_pin,OUTPUT);
    pinMode(motors[i].enable_pin,OUTPUT);
    // set the switch pin
    motors[i].limit_switch_state=HIGH;
    pinMode(motors[i].limit_switch_pin,INPUT);
    digitalWrite(motors[i].limit_switch_pin,HIGH);
    motors[i].limit_switch_state = digitalRead(motors[i].limit_switch_pin);
  }
  motor_enable();
  
  // disable global interrupts
  noInterrupts();
  // set entire TCCR1A register to 0
  TCCR1A = 0;
  // set the overflow clock to 0
  TCNT1  = 0;
  // set compare match register to desired timer count
  OCR1A = 2000;  // 1ms
  // turn on CTC mode
  TCCR1B = (1 << WGM12);
  // Set 8x prescaler
  TCCR1B |= ( 1 << CS11 );
  // enable timer compare interrupt
  TIMSK1 |= (1 << OCIE1A);
  
  interrupts();  // enable global interrupts
}


/**
 * Grips the power on the motors
 **/
void motor_enable() {
  int i;
  for(i=0;i<NUM_AXIES;++i) {
    digitalWrite(motors[i].enable_pin,LOW);
  }
}


/**
 * Releases the power on the motors
 **/
void motor_disable() {
  int i;
  for(i=0;i<NUM_AXIES;++i) {
    digitalWrite(motors[i].enable_pin,HIGH);
  }
}


void find_home() {
  // AXIS 1
  
  // hit switch
  while(digitalRead(motors[1].limit_switch_pin)==HIGH) {
    motor_onestep(1,1);
    pause(500);
  }
  // Back off switch
  while(digitalRead(motors[1].limit_switch_pin)==LOW) {
    motor_onestep(1,-1);
    pause(500);
  }
#if VERBOSE > 1
  Serial.println(F("Found 1"));
#endif

  // AXIS 2
  // hit switch
  while(digitalRead(motors[0].limit_switch_pin)==HIGH) {
    motor_onestep(0,-1);
    pause(500);
  }
  // Back off switch
  while(digitalRead(motors[0].limit_switch_pin)==LOW) {
    motor_onestep(0,1);
    pause(500);
  }
#if VERBOSE > 1
  Serial.println(F("Found 0"));
#endif

  // hit switch
  while(digitalRead(motors[2].limit_switch_pin)==HIGH) {
    motor_onestep(2,-1);
    pause(500);
  }
  // Back off switch
  while(digitalRead(motors[2].limit_switch_pin)==LOW) {
    motor_onestep(2,1);
    pause(500);
  }
#if VERBOSE > 1
  Serial.println(F("Found 2"));
#endif

  set_position(HOME_X,HOME_Y,HOME_Z);  // set staring position
  Serial.println(F("Found home."));
}


void motor_set_step_count(long a0,long a1,long a2) {  
  if( current_segment==last_segment ) {
    Segment &old_seg = line_segments[get_prev_segment(last_segment)];
    old_seg.a[0].step_count=a0;
    old_seg.a[1].step_count=a1;
    old_seg.a[2].step_count=a2;
    px=a0;
    py=a1;
    pz=a2;
  }
}


/**
* This file is part of Arm3.
*
* Arm3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Arm3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Foobar. If not, see <http://www.gnu.org/licenses/>.
*/
