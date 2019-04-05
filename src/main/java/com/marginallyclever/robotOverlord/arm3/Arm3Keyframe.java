package com.marginallyclever.robotOverlord.arm3;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;


/**
 * Describes the state of the robot at a given location.  This is so that states can be compared, rejected, etc for collision detection.
 * @author Dan Royer
 *
 */
public class Arm3Keyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9067108859208385388L;
	// angle of rotation
	public double angleBase = 0;
	public double angleShoulder = 0;
	public double angleElbow = 0;

	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3d fingerPosition;
	public Vector3d fingerForward;
	public Vector3d fingerRight;

	public double iku=0;
	public double ikv=0;
	public double ikw=0;
	
	public Vector3d wrist = new Vector3d();
	public Vector3d elbow = new Vector3d();
	public Vector3d shoulder = new Vector3d();
	
	public Vector3d base = new Vector3d();  // relative to world
	// base orientation, affects entire arm
	public Vector3d base_forward = new Vector3d();
	public Vector3d base_up = new Vector3d();
	public Vector3d base_right = new Vector3d();
	
	// rotating entire robot
	public double base_pan=0;
	public double base_tilt=0;
	
	public Arm3Dimensions dimensions;
	
	public Arm3Keyframe(Arm3Dimensions arg0) {
		dimensions = arg0;
		fingerPosition = arg0.getHomePosition();
		fingerForward = arg0.getHomeForward();
		fingerRight = arg0.getHomeRight();
	}
	
	public void set(Arm3Keyframe other) {
		angleBase = other.angleBase;
		angleShoulder = other.angleShoulder;
		angleElbow = other.angleElbow;
		iku=other.iku;
		ikv=other.ikv;
		ikw=other.ikw;
		fingerForward.set(other.fingerForward);
		fingerRight.set(other.fingerRight);
		fingerPosition.set(other.fingerPosition);
		wrist.set(other.wrist);
		elbow.set(other.elbow);
		shoulder.set(other.shoulder);
		base.set(other.base);
		base_forward.set(other.base_forward);
		base_up.set(other.base_up);
		base_right.set(other.base_right);
		base_pan = other.base_pan;
		base_tilt = other.base_tilt;
		dimensions = other.dimensions;
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
