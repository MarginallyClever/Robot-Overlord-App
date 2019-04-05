package com.marginallyclever.robotOverlord.arm5;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
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
	public double iku=0;
	public double ikv=0;
	public double ikw=0;
	// joint locations relative to base
	Vector3d wrist = new Vector3d();
	Vector3d elbow = new Vector3d();
	Vector3d boom = new Vector3d();
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
	Vector3d ik_wrist = new Vector3d();
	Vector3d ik_elbow = new Vector3d();
	Vector3d ik_boom = new Vector3d();
	Vector3d ik_shoulder = new Vector3d();
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