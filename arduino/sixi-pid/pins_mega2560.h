#pragma once

#if MOTHERBOARD == BOARD_MEGA2560

#define SIXI_UNIT1  // uncomment this for the very first sixi machine.

#define NUM_MOTORS                (6)
#define NUM_SERVOS                (1)
#define NUM_AXIES                 (NUM_MOTORS+NUM_SERVOS)
#define NUM_SENSORS               (6)

// MOTOR PINS

#define MOTOR_0_DIR_PIN           46
#define MOTOR_0_STEP_PIN          45
#define MOTOR_0_ENABLE_PIN        47

#define MOTOR_1_DIR_PIN           43
#define MOTOR_1_STEP_PIN          42
#define MOTOR_1_ENABLE_PIN        44

#define MOTOR_2_DIR_PIN           40
#define MOTOR_2_STEP_PIN          39
#define MOTOR_2_ENABLE_PIN        41

#define MOTOR_3_DIR_PIN           37
#define MOTOR_3_STEP_PIN          36
#define MOTOR_3_ENABLE_PIN        38

#define MOTOR_4_DIR_PIN           34
#define MOTOR_4_STEP_PIN          33
#define MOTOR_4_ENABLE_PIN        35

#define MOTOR_5_DIR_PIN           31
#define MOTOR_5_STEP_PIN          30
#define MOTOR_5_ENABLE_PIN        32

#define SERVO0_PIN                (13)



// SENSOR PINS

#define PIN_SENSOR_CSEL_0   8
#define  PIN_SENSOR_CLK_0   9
#define PIN_SENSOR_MOSI_0   10
#define PIN_SENSOR_MISO_0   11

#define PIN_SENSOR_CSEL_1   2
#define  PIN_SENSOR_CLK_1   3
#define PIN_SENSOR_MOSI_1   4
#define PIN_SENSOR_MISO_1   5

#define PIN_SENSOR_CSEL_2   17
#define  PIN_SENSOR_CLK_2   16
#define PIN_SENSOR_MOSI_2   15
#define PIN_SENSOR_MISO_2   14

#define PIN_SENSOR_CSEL_3   21
#define  PIN_SENSOR_CLK_3   20
#define PIN_SENSOR_MOSI_3   19
#define PIN_SENSOR_MISO_3   18

#ifdef SIXI_UNIT1

#define PIN_SENSOR_CSEL_4   29
#define  PIN_SENSOR_CLK_4   27
#define PIN_SENSOR_MOSI_4   25
#define PIN_SENSOR_MISO_4   23

#define PIN_SENSOR_CSEL_5   22
#define  PIN_SENSOR_CLK_5   24
#define PIN_SENSOR_MOSI_5   26
#define PIN_SENSOR_MISO_5   28

#else  // not SIXI_UNIT1

#define PIN_SENSOR_CSEL_4   22
#define  PIN_SENSOR_CLK_4   24
#define PIN_SENSOR_MOSI_4   26
#define PIN_SENSOR_MISO_4   28

#define PIN_SENSOR_CSEL_5   29
#define  PIN_SENSOR_CLK_5   27
#define PIN_SENSOR_MOSI_5   25
#define PIN_SENSOR_MISO_5   23

#endif  // SIXI_UNIT1

#endif
