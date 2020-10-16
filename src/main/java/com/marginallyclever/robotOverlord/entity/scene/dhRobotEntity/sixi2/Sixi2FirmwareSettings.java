package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

/**
 * taken directly from the arduino firmware
 * @author Dan Royer
 */
public class Sixi2FirmwareSettings {
	public static final double SENSOR_RESOLUTION = 360.0 / Math.pow(2,14); 
	
	public static final double MAX_FEEDRATE = 80.0;  // cm/s
	public static final double DEFAULT_FEEDRATE = 25.0;  // cm/s
	public static final double MAX_ACCELERATION = 202.5;  // cm/s
	public static final double DEFAULT_ACCELERATION = 25.25;  // cm/s/s
	
	public static final double MOTOR_STEPS_PER_TURN  =200.0; // motor full steps * microstepping setting
	
	public static final double NEMA17_CYCLOID_GEARBOX_RATIO        = 20.0;
	public static final double NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW  = 35.0;
	public static final double NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR = 30.0;
	public static final double NEMA24_CYCLOID_GEARBOX_RATIO        = 40.0;
	
	public static final double DM322T_MICROSTEP = 2.0;
	
	public static final double ELBOW_DOWNGEAR_RATIO = 30.0/20.0;
	public static final double NEMA17_RATIO         = DM322T_MICROSTEP*NEMA17_CYCLOID_GEARBOX_RATIO*ELBOW_DOWNGEAR_RATIO;
	public static final double NEMA23_RATIO_ELBOW   = NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW;
	public static final double NEMA23_RATIO_ANCHOR  = NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR;
	public static final double NEMA24_RATIO         = NEMA24_CYCLOID_GEARBOX_RATIO;
	
	// Motors are numbered 0 (base) to 5 (hand)
	public static final double MOTOR_0_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA23_RATIO_ANCHOR;  // anchor
	public static final double MOTOR_1_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA24_RATIO;  // shoulder
	public static final double MOTOR_2_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA23_RATIO_ELBOW;  // elbow
	public static final double MOTOR_3_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA17_RATIO;  // ulna
	public static final double MOTOR_4_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA17_RATIO;  // wrist
	public static final double MOTOR_5_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA17_RATIO;  // hand
	
	public static final double DEGREES_PER_STEP_0 = 360.0/MOTOR_0_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_1 = 360.0/MOTOR_1_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_2 = 360.0/MOTOR_2_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_3 = 360.0/MOTOR_3_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_4 = 360.0/MOTOR_4_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_5 = 360.0/MOTOR_5_STEPS_PER_TURN;
	
	public static final int MAX_SEGMENTS = 16;
	public static final double MIN_SEGMENT_TIME = 0.025;  // seconds

	public static final double MAX_JOINT_ACCELERATION = 101.5;  // deg/s
	public static final double MAX_JOINT_FEEDRATE = 50.0;  // deg/s

	public static final double [] MAX_JERK = { 3,3,3,4,4,5,5 };
}
