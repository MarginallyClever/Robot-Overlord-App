#pragma once

// Sixi 2 DH parameter table
#define DH_0_THETA 0
#define DH_0_ALPHA -90
#define DH_0_D     19.745
#define DH_0_R     0
#define DH_0_MAX   120
#define DH_0_MIN   -120

#define DH_1_THETA -90
#define DH_1_ALPHA 0
#define DH_1_D     0
#define DH_1_R     35.796
#define DH_1_MAX   0
#define DH_1_MIN   -170

#define DH_2_THETA 0
#define DH_2_ALPHA -90
#define DH_2_D     0
#define DH_2_R     6.426
#define DH_2_MAX   86
#define DH_2_MIN   -83.369

#define DH_3_THETA 0
#define DH_3_ALPHA 90
#define DH_3_D     38.705
#define DH_3_R     0
#define DH_3_MAX   175
#define DH_3_MIN   -175

#define DH_4_THETA 0
#define DH_4_ALPHA -90
#define DH_4_D     0
#define DH_4_R     0
#define DH_4_MAX   120
#define DH_4_MIN   -120

#define DH_5_THETA 0
#define DH_5_ALPHA 0
#define DH_5_D     5.795
#define DH_5_R     0
#define DH_5_MAX   170
#define DH_5_MIN   -170


class Kinematics {
public:
  uint8_t FK(float *jointAngles,float *cartesian);
  uint8_t IK(float *jointAngles,float *cartesian);
  void anglesToSteps(const float *const angles, int32_t *steps);
};

extern Kinematics kinematics;
