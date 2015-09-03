//------------------------------------------------------------------------------
// Arm3 - Three Axis Robot Arm based on code from 4 Axis CNC Demo v2
// dan@marginallycelver.com 2014-03-21
//------------------------------------------------------------------------------
// Copyright at end of file.
// please see http://www.marginallyclever.com/ for more information.


//------------------------------------------------------------------------------
// INCLUDES
//------------------------------------------------------------------------------
#include <Wire.h>
#include <Adafruit_MotorShield.h>


//------------------------------------------------------------------------------
// GLOBALS
//------------------------------------------------------------------------------
// Initialize Adafruit stepper controller
Adafruit_MotorShield AFMS0 = Adafruit_MotorShield(0x63);
Adafruit_MotorShield AFMS1 = Adafruit_MotorShield(0x60);
// Connect stepper motors with 400 steps per revolution (1.8 degree)
// Create the motor shield object with the default I2C address
Adafruit_StepperMotor *m[4];


//------------------------------------------------------------------------------
// METHODS
//------------------------------------------------------------------------------


void motor_setup() {
  AFMS0.begin(); // Start the shieldS
  AFMS1.begin();
  
  m[0] = AFMS0.getStepper(STEPS_PER_TURN, 1);
  m[1] = AFMS0.getStepper(STEPS_PER_TURN, 2);
  m[2] = AFMS1.getStepper(STEPS_PER_TURN, 1);
  m[3] = AFMS1.getStepper(STEPS_PER_TURN, 2);
}


/**
 * Supports movement with both styles of Motor Shield
 * @input newx the destination x position
 * @input newy the destination y position
 **/
void motor_onestep(int motor,int dir) {
#ifdef VERBOSE
  char *letter="XYZE";
  Serial.print(letter[motor]);
#endif
  m[motor]->onestep(dir>0?FORWARD:BACKWARD,STEP_TYPE);
}


void motor_disable() {
  int i;
  for(i=0;i<4;++i) {
    m[i]->release();
  }
}


void motor_enable() {
  int i;
  for(i=0;i<4;++i) {
    m[i]->onestep(BACKWARD,STEP_TYPE);
    m[i]->onestep(FORWARD,STEP_TYPE);
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
