package com.marginallyclever.robotOverlord.deltaRobot3;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.robot.RobotKeyframe;


/**
 * MotionState captures the physical state of a robot at a moment in time.
 * @author Dan Royer
 *
 */
public class DeltaRobot3Keyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = -772543196787298414L;

	// angle of rotation
	public DeltaRobot3Arm arms[];

	// Relative to base
	public Vector3f fingerPosition = new Vector3f(0,0,0);

	// base orientation, affects entire arm
	public Vector3f base = new Vector3f();  // relative to world
	public Vector3f base_forward = new Vector3f();
	public Vector3f base_up = new Vector3f();
	public Vector3f base_right = new Vector3f();

	// rotating entire robot
	public float basePan=0;
	public float baseTilt=0;


	public DeltaRobot3Keyframe() {
		arms = new DeltaRobot3Arm[DeltaRobot3.NUM_ARMS];
		int i;
		for(i=0;i<DeltaRobot3.NUM_ARMS;++i) {
			arms[i] = new DeltaRobot3Arm();
		}
	}

	public void set(DeltaRobot3Keyframe other) {
		fingerPosition.set(other.fingerPosition);
		int i;
		for(i=0;i<DeltaRobot3.NUM_ARMS;++i) {
			arms[i].set(other.arms[i]);
		}
		base.set(other.base);
		base_forward.set(other.base_forward);
		base_up.set(other.base_up);
		base_right.set(other.base_right);
		basePan = other.basePan;
		baseTilt = other.baseTilt;
	}
};
