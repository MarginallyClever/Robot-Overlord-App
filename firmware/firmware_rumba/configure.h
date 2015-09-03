#ifndef CONFIG_H
#define CONFIG_H

//------------------------------------------------------------------------------
// Arm3 - Three Axis Robot Arm based on code from 6 Axis CNC Demo v2
// dan@marginallycelver.com 2014-03-23
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.marginallyclever.com/ for more information.


//------------------------------------------------------------------------------
// CONSTANTS
//------------------------------------------------------------------------------
#define VERBOSE              (0)  // increase this number to get more output

// serial comms
#define BAUD                 (57600)  // How fast is the Arduino talking?
#define MAX_BUF              (64)  // What is the longest message Arduino can store?

// speeds & acceleration control
#define MIN_STEP_DELAY       (100) // fastest the motors can move
#define MAX_FEEDRATE         (40000.0)  // depends on timer interrupt & hardware
#define MIN_FEEDRATE         (1500)
#define DEFAULT_FEEDRATE     (8000.0)
#define DEFAULT_ACCELERATION (1000)   // how much to accelerate/decelerate

// related to number of instructions that can be buffered.  must be a power of two > 1.
#define MAX_SEGMENTS         (32)
// split long lines into pieces to make them more correct.
#define MM_PER_SEGMENT       (3)
#define MAX_TOOLS            (6)

// machine dimensions
#define BASE_TO_SHOULDER_X   (5.37)  // measured in solidworks
#define BASE_TO_SHOULDER_Z   (9.55)  // measured in solidworks
#define SHOULDER_TO_ELBOW    (25.0)
#define ELBOW_TO_WRIST       (25.0)
#define WRIST_TO_FINGER      (4.0)
#define FINGER_TO_FLOOR      (0.5)
#define STEP_MICROSTEPPING   (16.0)  // microstepping
#define MOTOR_STEPS_PER_TURN (400.0)
#define GEAR_RATIO           (5.0)
#define STEPS_PER_TURN       (MOTOR_STEPS_PER_TURN * STEP_MICROSTEPPING * GEAR_RATIO)

// arduino pins for motor control
#define MOTHERBOARD 1  // RUMBA
//#define MOTHERBOARD 2  // RAMPS 1.4

#if MOTHERBOARD == 1  // RUMBA
#define NUM_AXIES          (3)  // can go up to 6


#define MOTOR_0_DIR_PIN    (16)
#define MOTOR_0_STEP_PIN   (17)
#define MOTOR_0_ENABLE_PIN (48)
#define MOTOR_0_LIMIT_PIN  (37)

#define MOTOR_1_DIR_PIN    (47)
#define MOTOR_1_STEP_PIN   (54)
#define MOTOR_1_ENABLE_PIN (55)
#define MOTOR_1_LIMIT_PIN  (36)

#define MOTOR_2_DIR_PIN    (56)
#define MOTOR_2_STEP_PIN   (57)
#define MOTOR_2_ENABLE_PIN (62)
#define MOTOR_2_LIMIT_PIN  (35)

#define MOTOR_3_DIR_PIN    (22)
#define MOTOR_3_STEP_PIN   (23)
#define MOTOR_3_ENABLE_PIN (27)
#define MOTOR_3_LIMIT_PIN  (34)

#define MOTOR_4_DIR_PIN    (25)
#define MOTOR_4_STEP_PIN   (26)
#define MOTOR_4_ENABLE_PIN (24)
#define MOTOR_4_LIMIT_PIN  (33)

#define MOTOR_5_DIR_PIN    (28)
#define MOTOR_5_STEP_PIN   (29)
#define MOTOR_5_ENABLE_PIN (39)
#define MOTOR_5_LIMIT_PIN  (32)
#endif

#if MOTHERBOARD == 2  // RAMPS 1.4
#define NUM_AXIES          (3)  // can go up to 4.

#define MOTOR_0_DIR_PIN    (55)
#define MOTOR_0_STEP_PIN   (54)
#define MOTOR_0_ENABLE_PIN (38)
#define MOTOR_0_LIMIT_PIN  (3)

#define MOTOR_1_DIR_PIN    (61)
#define MOTOR_1_STEP_PIN   (60)
#define MOTOR_1_ENABLE_PIN (56)
#define MOTOR_1_LIMIT_PIN  (14)

#define MOTOR_2_DIR_PIN    (48)
#define MOTOR_2_STEP_PIN   (46)
#define MOTOR_2_ENABLE_PIN (62)
#define MOTOR_2_LIMIT_PIN  (18)

#define MOTOR_3_DIR_PIN    (28)
#define MOTOR_3_STEP_PIN   (26)
#define MOTOR_3_ENABLE_PIN (24)
#endif





// math defines
#define TWOPI                (PI*2)
#define RAD2DEG              (180.0/PI)

// EEPROM settings
#define EEPROM_VERSION       (3)  // firmware version
#define ADDR_VERSION         (0)
#define ADDR_UUID            (4)


// calibration settings
//*
#define HOME_Y               (0)
//#define HOME_X               (13.3)
//#define HOME_Z               (23.91+FINGER_TO_FLOOR)
#define HOME_X               (12.850)  // WAS 13.05
#define HOME_Z               (22.2)

//*/
/*
#define HOME_X (25.0+WRIST_TO_FINGER)
#define HOME_Y 0
#define HOME_Z -0.5
//*/


// time passed with no instruction?  Make sure PC knows we are waiting.
#define TIMEOUT_OK           (1000)
// timer stuff
#define CLOCK_FREQ           (16000000L)
#define MAX_COUNTER          (65536L)
// optimize code, please
#define FORCE_INLINE         __attribute__((always_inline)) inline


#ifndef CRITICAL_SECTION_START
  #define CRITICAL_SECTION_START  unsigned char _sreg = SREG;  cli();
  #define CRITICAL_SECTION_END    SREG = _sreg;
#endif //CRITICAL_SECTION_START


//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
#include "Vector3.h"


//------------------------------------------------------------------------------
// STRUCTS
//------------------------------------------------------------------------------
// for line()
typedef struct {
  int step_count;
  int delta;
  int absdelta;
  int dir;
  float delta_normalized;
} Axis;


typedef struct {
  int step_pin;
  int dir_pin;
  int enable_pin;
  int limit_switch_pin;
  int limit_switch_state;
  int flip;
} Motor;


typedef struct {
  Axis a[NUM_AXIES];
  int steps_total;
  int steps_taken;
  int accel_until;
  int decel_after;
  float feed_rate_max;
  float feed_rate_start;
  float feed_rate_start_max;
  float feed_rate_end;
  char nominal_length_flag;
  char recalculate_flag;
  char busy;
} Segment;


extern Segment line_segments[MAX_SEGMENTS];
extern Segment *working_seg;
extern volatile int current_segment;
extern volatile int last_segment;
extern float acceleration;


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
#endif
