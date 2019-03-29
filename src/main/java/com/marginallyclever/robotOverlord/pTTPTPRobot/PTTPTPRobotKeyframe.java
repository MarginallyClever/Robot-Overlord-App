package com.marginallyclever.robotOverlord.pTTPTPRobot;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class PTTPTPRobotKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	
	// angle of rotation
	float angleF = 0;
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
	public float basePan=0;
	public float baseTilt=0;
	
	void set(PTTPTPRobotKeyframe other) {
		angleF = other.angleF;
		angleE = other.angleE;
		angleD = other.angleD;
		angleC = other.angleC;
		angleB = other.angleB;
		angleA = other.angleA;
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

	@Override
	public void interpolate(RobotKeyframe a, RobotKeyframe b, float t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GL2 gl2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderInterpolation(GL2 gl2, RobotKeyframe arg1) {
		// TODO Auto-generated method stub
		
	}
}