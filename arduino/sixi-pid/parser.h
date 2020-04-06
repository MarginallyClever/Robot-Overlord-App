#pragma once

extern uint32_t lastCmdTimeMs;    // prevent timeouts
extern uint32_t reportDelay;  // how long since last D17 sent out


/**
 * D22
 * reset home position to the current angle values.
 */
void sixiResetSensorOffsets();

/**
 * D17 report the 6 axis sensor values from the Sixi robot arm.
 */
extern void reportAllAngleValues();

/**
 * D18 copy sensor values to motor step positions.
 */
extern void copySensorsToMotorPositions();

/**
   prepares the input buffer to receive a new message and tells the serial connected device it is ready for more.
*/
extern void parserReady();


extern void serialUpdate();
