package com.marginallyclever.robotOverlord.arm5;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class EvilMinionKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	// angle of rotation
	float angleE = 0;
	float angleD = 0;
	float angleC = 0;
	float angleB = 0;
	float angleA = 0;
	float angleServo = 120;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3f fingerPosition = new Vector3f();
	public Vector3f fingerForward = new Vector3f();
	public Vector3f fingerRight = new Vector3f();
	// finger rotation
	public float iku=0;
	public float ikv=0;
	public float ikw=0;
	// joint locations relative to base
	Vector3f wrist = new Vector3f();
	Vector3f elbow = new Vector3f();
	Vector3f boom = new Vector3f();
	Vector3f shoulder = new Vector3f();
	
	public Vector3f anchorPosition = new Vector3f();  // relative to world
	// base orientation, affects entire arm
	public Vector3f baseForward = new Vector3f();
	public Vector3f baseUp = new Vector3f();
	public Vector3f baseRight = new Vector3f();
	
	// rotating entire robot
	float base_pan=0;
	float base_tilt=0;

	// inverse kinematics visualizations
	Vector3f ik_wrist = new Vector3f();
	Vector3f ik_elbow = new Vector3f();
	Vector3f ik_boom = new Vector3f();
	Vector3f ik_shoulder = new Vector3f();
	float ik_angleE = 0;
	float ik_angleD = 0;
	float ik_angleC = 0;
	float ik_angleB = 0;
	float ik_angleA = 0;
	
	
	void set(EvilMinionKeyframe other) {
		angleE = other.angleE;
		angleD = other.angleD;
		angleC = other.angleC;
		angleB = other.angleB;
		angleA = other.angleA;
		angleServo = other.angleServo;
		iku=other.iku;
		ikv=other.ikv;
		ikw=other.ikw;
		fingerForward.set(other.fingerForward);
		fingerRight.set(other.fingerRight);
		fingerPosition.set(other.fingerPosition);
		wrist.set(other.wrist);
		elbow.set(other.elbow);
		boom.set(other.boom);
		shoulder.set(other.shoulder);
		anchorPosition.set(other.anchorPosition);
		baseForward.set(other.baseForward);
		baseUp.set(other.baseUp);
		baseRight.set(other.baseRight);
		base_pan = other.base_pan;
		base_tilt = other.base_tilt;
		
		ik_angleA = other.ik_angleA;
		ik_angleB = other.ik_angleB;
		ik_angleC = other.ik_angleC;
		ik_angleD = other.ik_angleD;
		ik_angleE = other.ik_angleE;

		ik_wrist.set(other.ik_wrist);
		ik_elbow.set(other.ik_elbow);
		ik_boom.set(other.ik_boom);
		ik_shoulder.set(other.ik_shoulder);
	}
}