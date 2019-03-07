package com.marginallyclever.robotOverlord.sixi2Robot;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class Sixi2RobotKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	
	// angle of rotation
	float angle0 = 0;
	float angle1 = 0;
	float angle2 = 0;
	float angle3 = 0;
	float angle4 = 0;
	float angle5 = 0;
	float angleServo = 120;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3f fingerPosition = new Vector3f();
	public Vector3f fingerForward = new Vector3f();
	public Vector3f fingerRight = new Vector3f();
	// finger rotation, in degrees
	public float ikU=0;
	public float ikV=0;
	public float ikW=0;
	// joint locations relative to base
	public Vector3f wrist = new Vector3f();
	public Vector3f elbow = new Vector3f();
	public Vector3f bicep = new Vector3f();
	public Vector3f shoulder = new Vector3f();
	public Vector3f base = new Vector3f();
	
	public Vector3f anchorPosition = new Vector3f();  // relative to world
	// base orientation, affects entire arm
	public Vector3f baseForward = new Vector3f();
	public Vector3f baseUp = new Vector3f();
	public Vector3f baseRight = new Vector3f();
	
	// rotating entire robot
	public double basePan=0;
	public double baseTilt=0;
	
	void set(Sixi2RobotKeyframe other) {
		angle0 = other.angle0;
		angle1 = other.angle1;
		angle2 = other.angle2;
		angle3 = other.angle3;
		angle4 = other.angle4;
		angle5 = other.angle5;
		angleServo = other.angleServo;
		ikU=other.ikU;
		ikV=other.ikV;
		ikW=other.ikW;
		fingerForward.set(other.fingerForward);
		fingerRight.set(other.fingerRight);
		fingerPosition.set(other.fingerPosition);
		wrist.set(other.wrist);
		elbow.set(other.elbow);
		bicep.set(other.bicep);
		shoulder.set(other.shoulder);
		anchorPosition.set(other.anchorPosition);
		baseForward.set(other.baseForward);
		baseUp.set(other.baseUp);
		baseRight.set(other.baseRight);
		basePan = other.basePan;
		baseTilt = other.baseTilt;

		wrist.set(other.wrist);
		elbow.set(other.elbow);
		shoulder.set(other.shoulder);
		base.set(other.base);
	}
}