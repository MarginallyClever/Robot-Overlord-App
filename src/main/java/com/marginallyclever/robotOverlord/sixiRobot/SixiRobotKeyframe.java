package com.marginallyclever.robotOverlord.sixiRobot;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A snapshot in time of the robot in a given position.  Can run forward or inverse kinematics to calcualte the joint angles and/or the tool position.
 * @author danroyer
 *
 */
class SixiRobotKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1012199745425607761L;
	
	// angle of rotation
	double angle0 = 0;
	double angle1 = 0;
	double angle2 = 0;
	double angle3 = 0;
	double angle4 = 0;
	double angle5 = 0;
	double angleServo = 120;
	
	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3d fingerPosition = new Vector3d();
	public Vector3d fingerForward = new Vector3d();
	public Vector3d fingerRight = new Vector3d();
	// finger rotation, in degrees
	public double ikU=0;
	public double ikV=0;
	public double ikW=0;
	// joint locations relative to base
	public Vector3d wrist = new Vector3d();
	public Vector3d elbow = new Vector3d();
	public Vector3d bicep = new Vector3d();
	public Vector3d shoulder = new Vector3d();
	public Vector3d base = new Vector3d();
	
	public Vector3d anchorPosition = new Vector3d();  // relative to world
	// base orientation, affects entire arm
	public Vector3d baseForward = new Vector3d();
	public Vector3d baseUp = new Vector3d();
	public Vector3d baseRight = new Vector3d();
	
	// rotating entire robot
	public double basePan=0;
	public double baseTilt=0;
	
	void set(SixiRobotKeyframe other) {
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