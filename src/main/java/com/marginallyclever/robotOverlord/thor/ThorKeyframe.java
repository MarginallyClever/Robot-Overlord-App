package com.marginallyclever.robotOverlord.thor;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class ThorKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	// angle of rotation
	double angleF = 0;
	double angleE = 0;
	double angleD = 0;
	double angleC = 0;
	double angleB = 0;
	double angleA = 0;
	double angleServo = 120;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3d fingerPosition = new Vector3d();
	public Vector3d fingerForward = new Vector3d();
	public Vector3d fingerRight = new Vector3d();
	// finger rotation
	public double ikU=0;
	public double ikV=0;
	public double ikW=0;
	// joint locations relative to base
	Vector3d wrist = new Vector3d();
	Vector3d elbow = new Vector3d();
	Vector3d bicep = new Vector3d();
	Vector3d shoulder = new Vector3d();
	
	public Vector3d anchorPosition = new Vector3d();  // relative to world
	// base orientation, affects entire arm
	public Vector3d baseForward = new Vector3d();
	public Vector3d baseUp = new Vector3d();
	public Vector3d baseRight = new Vector3d();
	
	// rotating entire robot
	double base_pan=0;
	double base_tilt=0;

	// inverse kinematics visualizations
	public Vector3d ikWrist = new Vector3d();
	public Vector3d ikElbow = new Vector3d();
	public Vector3d ikShoulder = new Vector3d();
	public Vector3d ikBase = new Vector3d();
	double ik_angleF = 0;
	double ik_angleE = 0;
	double ik_angleD = 0;
	double ik_angleC = 0;
	double ik_angleB = 0;
	double ik_angleA = 0;
	
	
	void set(ThorKeyframe other) {
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
		base_pan = other.base_pan;
		base_tilt = other.base_tilt;

		ik_angleA = other.ik_angleA;
		ik_angleB = other.ik_angleB;
		ik_angleC = other.ik_angleC;
		ik_angleD = other.ik_angleD;
		ik_angleE = other.ik_angleE;
		ik_angleF = other.ik_angleF;

		ikWrist.set(other.ikWrist);
		ikElbow.set(other.ikElbow);
		ikShoulder.set(other.ikShoulder);
		ikBase.set(other.ikBase);
	}


	@Override
	public void interpolate(RobotKeyframe a, RobotKeyframe b, double t) {
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