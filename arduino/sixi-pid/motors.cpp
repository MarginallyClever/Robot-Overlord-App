//------------------------------------------------------------------------------
// Sixi PID firmware
// 2020-03-28 dan@marginallyclever.com
// CC-by-NC-SA
//------------------------------------------------------------------------------

#include "configure.h"
//#include <Servo.h>

StepperMotor motors[NUM_MOTORS];

#if NUM_SERVOS>0
Servo servos[NUM_SERVOS];
#endif
