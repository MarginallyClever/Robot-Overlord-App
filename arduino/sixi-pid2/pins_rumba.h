#pragma once

#if MOTHERBOARD == BOARD_RUMBA

#define NUM_MOTORS                (6)
#define NUM_SERVOS                (1)
#define NUM_SENSORS               (0)

// MOTOR PINS

#define MOTOR_0_DIR_PIN           (16)
#define MOTOR_0_STEP_PIN          (17)
#define MOTOR_0_ENABLE_PIN        (48)

#define MOTOR_1_DIR_PIN           (47)
#define MOTOR_1_STEP_PIN          (54)
#define MOTOR_1_ENABLE_PIN        (55)

#define MOTOR_2_DIR_PIN           (56)
#define MOTOR_2_STEP_PIN          (57)
#define MOTOR_2_ENABLE_PIN        (62)

#define MOTOR_3_DIR_PIN           (22)
#define MOTOR_3_STEP_PIN          (23)
#define MOTOR_3_ENABLE_PIN        (24)

#define MOTOR_4_DIR_PIN           (25)
#define MOTOR_4_STEP_PIN          (26)
#define MOTOR_4_ENABLE_PIN        (27)

#define MOTOR_5_DIR_PIN           (28)
#define MOTOR_5_STEP_PIN          (29)
#define MOTOR_5_ENABLE_PIN        (39)

#define SERVO0_PIN                (5)



// SENSOR PINS

#define PIN_SENSOR_CSEL_0   4
#define  PIN_SENSOR_CLK_0   4
#define PIN_SENSOR_MOSI_0   4
#define PIN_SENSOR_MISO_0   4

#define PIN_SENSOR_CSEL_1   4
#define  PIN_SENSOR_CLK_1   4
#define PIN_SENSOR_MOSI_1   4
#define PIN_SENSOR_MISO_1   4

#define PIN_SENSOR_CSEL_2   4
#define  PIN_SENSOR_CLK_2   4
#define PIN_SENSOR_MOSI_2   4
#define PIN_SENSOR_MISO_2   4

#define PIN_SENSOR_CSEL_3   4
#define  PIN_SENSOR_CLK_3   4
#define PIN_SENSOR_MOSI_3   4
#define PIN_SENSOR_MISO_3   4

#define PIN_SENSOR_CSEL_4   4
#define  PIN_SENSOR_CLK_4   4
#define PIN_SENSOR_MOSI_4   4
#define PIN_SENSOR_MISO_4   4

#define PIN_SENSOR_CSEL_5   4
#define  PIN_SENSOR_CLK_5   4
#define PIN_SENSOR_MOSI_5   4
#define PIN_SENSOR_MISO_5   4

#endif
