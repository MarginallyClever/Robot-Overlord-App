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
// GLOBALS
//------------------------------------------------------------------------------


// A ring buffer of line segments.
// TODO: optimize speed between line segments
Segment line_segments[MAX_SEGMENTS];
Segment *working_seg = NULL;
volatile int current_segment  = 0;
volatile int last_segment     = 0;
int step_multiplier, nominal_step_multiplier;
unsigned short nominal_OCR1A;

// used by timer1 to optimize interrupt inner loop
int delta[NUM_AXIES];
int over[NUM_AXIES];
int steps_total;
int steps_taken;
int accel_until,decel_after;
long current_feed_rate;
long old_feed_rate=0;
long start_feed_rate,end_feed_rate;
long time_accelerating,time_decelerating;


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------


// force this thread to do nothing until all the queued segments are processed.
void wait_for_segment_buffer_to_empty() {
  while( current_segment != last_segment );
}


/**
 * @return 1 if buffer is full, 0 if it is not.
 */
char segment_buffer_full() {
  int next_segment = get_next_segment(last_segment);
  return (next_segment == current_segment);
}


FORCE_INLINE int get_next_segment(int i) {
  return ( i + 1 ) & ( MAX_SEGMENTS - 1 );
}


FORCE_INLINE int get_prev_segment(int i) {
  return ( i + MAX_SEGMENTS - 1 ) & ( MAX_SEGMENTS - 1 );
}


float max_speed_allowed(float acceleration, float target_velocity, float distance) {
  //return sqrt(target_velocity*target_velocity - 2*acceleration*distance);
  return target_velocity - acceleration * distance;
}


void segment_setup() {
  current_segment=0;
  last_segment=0;
  Segment &old_seg = line_segments[get_prev_segment(last_segment)];
  for(int i=0;i<NUM_AXIES;++i) {
    old_seg.a[i].step_count=0;
  }
  working_seg = NULL;
}


// for reasons I don't understand... if i put this method in the .ino file i get compile errors.
// so I put it here, which forces the externs.
FORCE_INLINE Segment *segment_get_working() {
  if(current_segment == last_segment ) return NULL;
  working_seg = &line_segments[current_segment];
  working_seg->busy=true;
  return working_seg;
}


void recalculate_reverse2(Segment *prev,Segment *current,Segment *next) {
  if(current==NULL) return;
  if(next==NULL) return;
  
  if (current->feed_rate_start != current->feed_rate_start_max) {
    // If nominal length true, max junction speed is guaranteed to be reached. Only compute
    // for max allowable speed if block is decelerating and nominal length is false.
    if ((!current->nominal_length_flag) && (current->feed_rate_start_max > next->feed_rate_start)) {
      float v = min( current->feed_rate_start_max,
                     max_speed_allowed(-acceleration,next->feed_rate_start,current->steps_total));
      current->feed_rate_start = v;
    } else {
      current->feed_rate_start = current->feed_rate_start_max;
    }
    current->recalculate_flag = true;
  }
}


void recalculate_reverse() {
  int s = last_segment;
  Segment *blocks[3] = {NULL,NULL,NULL};
  
  while(s != current_segment) {
    s=get_prev_segment(s);
    blocks[2]=blocks[1];
    blocks[1]=blocks[0];
    blocks[0]=&line_segments[s];
    recalculate_reverse2(blocks[0],blocks[1],blocks[2]);
  }
}


void recalculate_forward2(Segment *prev,Segment *current,Segment *next) {
  if(prev==NULL) return;
  
  // If the previous block is an acceleration block, but it is not long enough to complete the
  // full speed change within the block, we need to adjust the entry speed accordingly. Entry
  // speeds have already been reset, maximized, and reverse planned by reverse planner.
  // If nominal length is true, max junction speed is guaranteed to be reached. No need to recheck.
  if (!prev->nominal_length_flag) {
    if (prev->feed_rate_start < current->feed_rate_start) {
      double feed_rate_start = min( current->feed_rate_start,
                                    max_speed_allowed(-acceleration,prev->feed_rate_start,prev->steps_total) );

      // Check for junction speed change
      if (current->feed_rate_start != feed_rate_start) {
        current->feed_rate_start = feed_rate_start;
        current->recalculate_flag = true;
      }
    }
  }
}


void recalculate_forward() {
  int s = current_segment;
  Segment *blocks[3] = {NULL,NULL,NULL};
  
  while(s != last_segment) {
    s=get_next_segment(s);
    blocks[0]=blocks[1];
    blocks[1]=blocks[2];
    blocks[2]=&line_segments[s];
    recalculate_forward2(blocks[0],blocks[1],blocks[2]);
  }
  recalculate_forward2(blocks[1],blocks[2],NULL);
}


int intersection_time(float acceleration,float distance,float start_speed,float end_speed) {
#if 0
  return ( ( 2.0*acceleration*distance - start_speed*start_speed + end_speed*end_speed ) / (4.0*acceleration) );
#else
  float t2 = ( start_speed - end_speed + acceleration * distance ) / ( 2.0 * acceleration );
  return distance - t2;
#endif
}


void segment_update_trapezoid(Segment *s,float start_speed,float end_speed) {
  if(start_speed<MIN_FEEDRATE) start_speed=MIN_FEEDRATE;
  if(end_speed<MIN_FEEDRATE) end_speed=MIN_FEEDRATE;
  
  //int steps_to_accel =  ceil( (s->feed_rate_max*s->feed_rate_max - start_speed*start_speed )/ (2.0*acceleration) );
  //int steps_to_decel = floor( (end_speed*end_speed - s->feed_rate_max*s->feed_rate_max )/ -(2.0*acceleration) );
  int steps_to_accel =  ceil( ( s->feed_rate_max - start_speed ) / acceleration );
  int steps_to_decel = floor( ( end_speed - s->feed_rate_max ) / -acceleration );

  int steps_at_top_speed = s->steps_total - steps_to_accel - steps_to_decel;

  if(steps_at_top_speed<=0) {
    steps_to_accel = ceil( intersection_time(acceleration,s->steps_total,start_speed,end_speed) );
    steps_at_top_speed=0;
    steps_to_accel = max(steps_to_accel,0);
    steps_to_accel = min(steps_to_accel,s->steps_total);
  }
/*
  Serial.print("M");  Serial.println(s->feed_rate_max);
  Serial.print("E");  Serial.println(end_speed);
  Serial.print("S");  Serial.println(start_speed);
  Serial.print("@");  Serial.println(acceleration);
  Serial.print("A");  Serial.println(steps_to_accel);
  Serial.print("D");  Serial.println(steps_to_decel);
*/
CRITICAL_SECTION_START
  if(s->busy==false) {
    s->accel_until = steps_to_accel;
    s->decel_after = steps_to_accel+steps_at_top_speed;
    s->feed_rate_start = start_speed;
    s->feed_rate_end = end_speed;
  }
CRITICAL_SECTION_END
}


void recalculate_trapezoids() {
  int s = current_segment;
  Segment *current;
  Segment *next = NULL;
  
  while(s != last_segment) {
    current = next;
    next = &line_segments[s];
    if (current) {
      // Recalculate if current block entry or exit junction speed has changed.
      if (current->recalculate_flag || next->recalculate_flag)
      {
        // NOTE: Entry and exit factors always > 0 by all previous logic operations.
        segment_update_trapezoid(current,current->feed_rate_start, next->feed_rate_start);
        current->recalculate_flag = false; // Reset current only to ensure next trapezoid is computed
      }
    }
    s=get_next_segment(s);
  }
  // Last/newest block in buffer. Make sure the last block always ends motion.
  if(next != NULL) {
    segment_update_trapezoid(next, next->feed_rate_start, MIN_FEEDRATE);
    next->recalculate_flag = false;
  }
}


void recalculate_acceleration() {
  recalculate_reverse();
  recalculate_forward();
  recalculate_trapezoids();

#if VERBOSE > 1
  //Serial.println("\nstart max,max,start,end,rate,total,up steps,cruise,down steps,nominal?");
  Serial.println("---------------");
  int s = current_segment;
  
  while(s != last_segment) {
    Segment *next = &line_segments[s];
    s=get_next_segment(s);
//                             Serial.print(next->feed_rate_start_max);
//    Serial.print(F("\t"));   Serial.print(next->feed_rate_max);
//    Serial.print(F("\t"));   Serial.print(acceleration);
    Serial.print(F("\tS"));  Serial.print(next->feed_rate_start);
//    Serial.print(F("\tE"));  Serial.print(next->feed_rate_end);
    Serial.print(F("\t*"));  Serial.print(next->steps_total);
    Serial.print(F("\tA"));  Serial.print(next->accel_until);
    int after = (next->steps_total - next->decel_after);
    int total = next->steps_total - after - next->accel_until;
    Serial.print(F("\tT"));  Serial.print(total);
    Serial.print(F("\tD"));  Serial.print(after);
    Serial.print(F("\t"));   Serial.println(next->nominal_length_flag==1?'*':' ');
  }
#endif
}


/**
 * Set the clock 1 timer frequency.
 * @input desired_freq_hz the desired frequency
 */
FORCE_INLINE unsigned short calc_timer(long desired_freq_hz) {
  if( desired_freq_hz > MAX_FEEDRATE ) desired_freq_hz = MAX_FEEDRATE;
  if( desired_freq_hz < MIN_FEEDRATE ) desired_freq_hz = MIN_FEEDRATE;
  old_feed_rate = desired_freq_hz;
  
  // Source: https://github.com/MarginallyClever/ArduinoTimerInterrupt
  // Different clock sources can be selected for each timer independently. 
  // To calculate the timer frequency (for example 2Hz using timer1) you will need:
  
  if(desired_freq_hz > 20000 ) {
    step_multiplier = 4;
    desired_freq_hz >>= 2;
  } else if(desired_freq_hz > 10000 ) {
    step_multiplier = 2;
    desired_freq_hz >>= 1;
  } else {
    step_multiplier=1;
  }

  long counter_value = ( CLOCK_FREQ / 8 ) / desired_freq_hz;
  if( counter_value >= MAX_COUNTER ) {
    //Serial.print("this breaks the timer and crashes the arduino");
    //Serial.flush();
    counter_value = MAX_COUNTER - 1;
  } else if( counter_value < 100 ) {
    counter_value = 100;
  }

  return counter_value;
}


/**
 * Process all line segments in the ring buffer.  Uses bresenham's line algorithm to move all motors.
 */
ISR(TIMER1_COMPA_vect) {
  // segment buffer empty? do nothing
  if( working_seg == NULL ) {
    working_seg = segment_get_working();
    if( working_seg != NULL ) {
      // New segment!
      // set the direction pins
      digitalWrite( MOTOR_0_DIR_PIN, working_seg->a[0].dir );
      digitalWrite( MOTOR_1_DIR_PIN, working_seg->a[1].dir );
      digitalWrite( MOTOR_2_DIR_PIN, working_seg->a[2].dir );
#if NUM_AXIES >3
      digitalWrite( MOTOR_3_DIR_PIN, working_seg->a[3].dir );
#endif
#if NUM_AXIES >4
      digitalWrite( MOTOR_4_DIR_PIN, working_seg->a[4].dir );
#endif
#if NUM_AXIES >5
      digitalWrite( MOTOR_5_DIR_PIN, working_seg->a[5].dir );
#endif

      // set frequency to segment feed rate
      nominal_OCR1A = calc_timer(working_seg->feed_rate_max);
      nominal_step_multiplier = step_multiplier;
      
      start_feed_rate = working_seg->feed_rate_start;
      end_feed_rate = working_seg->feed_rate_end;
      current_feed_rate = start_feed_rate;
      time_decelerating = 0;
      time_accelerating = calc_timer(start_feed_rate);
      OCR1A = time_accelerating;
      
      // defererencing some data so the loop runs faster.
      steps_total=working_seg->steps_total;
      steps_taken=0;
      delta[0] = working_seg->a[0].absdelta;
      delta[1] = working_seg->a[1].absdelta;
      delta[2] = working_seg->a[2].absdelta;
#if NUM_AXIES >3
      delta[3] = working_seg->a[3].absdelta;
#endif
#if NUM_AXIES >4
      delta[4] = working_seg->a[4].absdelta;
#endif
#if NUM_AXIES >5
      delta[5] = working_seg->a[5].absdelta;
#endif
      memset(over,0,sizeof(int)*NUM_AXIES);
      accel_until=working_seg->accel_until;
      decel_after=working_seg->decel_after;
      return;
    } else {
      OCR1A = 2000; // wait 1ms
      return;
    }
  }
  
  if( working_seg != NULL ) {
    // move each axis
    for(int i=0;i<step_multiplier;++i) {
      // M0
      over[0] += delta[0];
      if(over[0] >= steps_total) {
        digitalWrite(MOTOR_0_STEP_PIN,LOW);
        over[0] -= steps_total;
        digitalWrite(MOTOR_0_STEP_PIN,HIGH);
      }
      // M1
      over[1] += delta[1];
      if(over[1] >= steps_total) {
        digitalWrite(MOTOR_1_STEP_PIN,LOW);
        over[1] -= steps_total;
        digitalWrite(MOTOR_1_STEP_PIN,HIGH);
      }
      // M2
      over[2] += delta[2];
      if(over[2] >= steps_total) {
        digitalWrite(MOTOR_2_STEP_PIN,LOW);
        over[2] -= steps_total;
        digitalWrite(MOTOR_2_STEP_PIN,HIGH);
      }
#if NUM_AXIES >3
      // M3
      over[3] += delta[3];
      if(over[3] >= steps_total) {
        digitalWrite(MOTOR_3_STEP_PIN,LOW);
        over[3] -= steps_total;
        digitalWrite(MOTOR_3_STEP_PIN,HIGH);
      }
#endif
#if NUM_AXIES >4
      // M4
      over[4] += delta[4];
      if(over[4] >= steps_total) {
        digitalWrite(MOTOR_4_STEP_PIN,LOW);
        over[4] -= steps_total;
        digitalWrite(MOTOR_4_STEP_PIN,HIGH);
      }
#endif
#if NUM_AXIES >5
      // M5
      over[5] += delta[5];
      if(over[5] >= steps_total) {
        digitalWrite(MOTOR_5_STEP_PIN,LOW);
        over[5] -= steps_total;
        digitalWrite(MOTOR_5_STEP_PIN,HIGH);
      }
#endif
    
      // make a step
      steps_taken++;
      
      if(steps_taken >= steps_total) break;
    }
    
    // accel
    unsigned short t;
    if( steps_taken <= accel_until ) {
      current_feed_rate = (acceleration * time_accelerating / 1000000);
      current_feed_rate += start_feed_rate;
      t = calc_timer(current_feed_rate);
      OCR1A = t;
      time_accelerating+=t;
    } else if( steps_taken > decel_after ) {
      unsigned short step_time = (acceleration * time_decelerating / 1000000);
      long end_feed_rate = current_feed_rate - step_time;
      t = calc_timer(end_feed_rate);
      OCR1A = t;
      time_decelerating+=t;
    } else {
      OCR1A = nominal_OCR1A;
      step_multiplier = nominal_step_multiplier;
    }

    // Is this segment done?
    if( steps_taken >= steps_total ) {
      // Move on to next segment without wasting an interrupt tick.
      working_seg = NULL;
      current_segment = get_next_segment(current_segment);
    }
  }
}


/**
 * Add a segment to the line buffer when there is room.
 */
void segment_prepare(int n0,int n1,int n2,float new_feed_rate) {
  // get the next available spot in the segment buffer
  int next_segment = get_next_segment(last_segment);
  while( next_segment == current_segment ) {
    // the buffer is full, we are way ahead of the motion system
    delay(1);
  }

  int prev_segment = get_prev_segment(last_segment);
  Segment &new_seg = line_segments[last_segment];
  Segment &old_seg = line_segments[prev_segment];

  new_seg.a[0].step_count = n0;
  new_seg.a[1].step_count = n1;
  new_seg.a[2].step_count = n2;
  new_seg.a[0].delta = n0 - old_seg.a[0].step_count;
  new_seg.a[1].delta = n1 - old_seg.a[1].step_count;
  new_seg.a[2].delta = n2 - old_seg.a[2].step_count;
  new_seg.feed_rate_max = new_feed_rate;

  // the axis that has the most steps will control the overall acceleration
  new_seg.steps_total = 0;
  float len = 0;
  int i;
  for(i=0;i<NUM_AXIES;++i) {
    new_seg.a[i].dir = ( new_seg.a[i].delta * motors[0].flip < 0 ? HIGH : LOW );
    new_seg.a[i].absdelta = abs(new_seg.a[i].delta);
    len += new_seg.a[i].absdelta * new_seg.a[i].absdelta;
    if( new_seg.steps_total < new_seg.a[i].absdelta ) {
      new_seg.steps_total = new_seg.a[i].absdelta;
    }
  }

  // No steps?  No work!  Stop now.
  if( new_seg.steps_total == 0 ) return;
  
  len = sqrt( len );
  float ilen = 1.0f / len;
  for(i=0;i<NUM_AXIES;++i) {
    new_seg.a[i].delta_normalized = new_seg.a[i].delta * ilen;
  }
  new_seg.steps_taken = 0;

  // what is the maximum starting speed for this segment?
  float feed_rate_start_max = MIN_FEEDRATE;
    // is the robot changing direction sharply?
      // aka is there a previous segment with a wildly different delta_normalized?
    if(last_segment != current_segment) {
      float cos_theta = 0;
      for(i=0;i<NUM_AXIES;++i) {
        cos_theta += new_seg.a[i].delta_normalized * old_seg.a[i].delta_normalized;
      }
      
      feed_rate_start_max = min( new_seg.feed_rate_max, old_seg.feed_rate_max );
      if(cos_theta<0.95) {
        if(cos_theta<0) cos_theta = 0;
        feed_rate_start_max *= cos_theta;
      }
    }

  float allowable_speed = max_speed_allowed(-acceleration, MIN_FEEDRATE, new_seg.steps_total);
  //Serial.print("max = ");  Serial.println(feed_rate_start_max);
//  Serial.print("allowed = ");  Serial.println(allowable_speed);
  new_seg.feed_rate_start_max = feed_rate_start_max;
  new_seg.feed_rate_start = min(feed_rate_start_max, allowable_speed);

  new_seg.nominal_length_flag = ( allowable_speed >= new_seg.feed_rate_max );
  new_seg.recalculate_flag = true;
  new_seg.busy=false;
  
  // when should we accelerate and decelerate in this segment? 
  segment_update_trapezoid(&new_seg,new_seg.feed_rate_start,MIN_FEEDRATE);

  recalculate_acceleration();
  
  last_segment = next_segment;
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
