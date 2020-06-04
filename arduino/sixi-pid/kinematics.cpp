#include "configure.h"

Kinematics kinematics;


/**
   Forward Kinematics - turns joint angles into end effector cartesian coordinates.
   @param jointAngles angles in degrees
   @param cartesian cartesian coordinate
   @return 0 if no problem, 1 on failure.
*/
uint8_t Kinematics::FK(float *jointAngles,float *cartesian) {
  // TODO fill me in!

  return 0;
}


/**
   Inverse Kinematics - turns end effector cartesian coordinates into joint angles.
   Uses Gradient Descent to adjust jointAngles towards the desired cartesian pose.  like.... wiggling until the arm gets closer.
   @param jointAngles angles in degrees.  
   @param cartesian cartesian coordinate
   @return 0 if no problem, 1 on failure.
*/
uint8_t Kinematics::IK(float *jointAngles,float *cartesian) {
  // TODO fill me in!

  return 0;
}


/**
 * Turns angle values into step counts for each motor
 * @param angles in degrees
 * @param steps motor pulses to reach the same angle
*/
void Kinematics::anglesToSteps(const float *const angles, int32_t *steps) {
  // each of the xyz motors are differential to each other.
  // to move only one motor means applying the negative of that value to the other two motors

  // consider a two motor differential:
  // if x moves, subtract x from y.
  // if y moves, subtract y from x.
  // so for three axis,
  // for any axis N subtract the other two axies from this axis.

  // Some of these are negative because the motor is wired to turn the opposite direction from the Robot Overlord simulation.
  // Robot Overlord has the final say, so these are flipped to match the simulation.
  // This is the only place motor direction should ever be inverted.
  float J0 = -angles[0];  // anchor  (G0 X*)
  float J1 =  angles[1];  // shoulder (G0 Y*)
  float J2 =  angles[2];  // elbow (G0 Z*)
  float J3 = -angles[3];  // ulna  (G0 U*)
  float J4 =  angles[4];  // wrist (G0 V*)
  float J5 = -angles[5];  // hand  (G0 W*)

  // adjust for the wrist differential
  J5 += (J4 / NEMA17_CYCLOID_GEARBOX_RATIO) + (J3 / NEMA17_CYCLOID_GEARBOX_RATIO);
  J4 += (J3 / NEMA17_CYCLOID_GEARBOX_RATIO);

  steps[0] = J0 * STEP_PER_DEGREES_0;  // ANCHOR
  steps[1] = J1 * STEP_PER_DEGREES_1;  // SHOULDER
  steps[2] = J2 * STEP_PER_DEGREES_2;  // ELBOW
  steps[3] = J3 * STEP_PER_DEGREES_3;  // ULNA
  steps[4] = J4 * STEP_PER_DEGREES_4;  // WRIST
  steps[5] = J5 * STEP_PER_DEGREES_5;  // HAND

  //  steps[NUM_MOTORS] = angles[6];
#ifdef DEBUG_anglesToSteps
  Serial.print("J=");  Serial.print(J0);
  Serial.print('\t');  Serial.print(J1);
  Serial.print('\t');  Serial.print(J2);
  Serial.print('\t');  Serial.print(J3);
  Serial.print('\t');  Serial.print(J4);
  Serial.print('\t');  Serial.print(J5);
  Serial.print('\n');
#endif
}
