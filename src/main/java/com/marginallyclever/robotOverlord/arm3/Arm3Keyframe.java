package com.marginallyclever.robotOverlord.arm3;

import javax.vecmath.Vector3f;

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
	public float angleBase = 0;
	public float angleShoulder = 0;
	public float angleElbow = 0;

	// robot arm coordinates.  Relative to base unless otherwise noted.
	public Vector3f fingerPosition;
	public Vector3f fingerForward;
	public Vector3f fingerRight;

	public float iku=0;
	public float ikv=0;
	public float ikw=0;
	
	public Vector3f wrist = new Vector3f();
	public Vector3f elbow = new Vector3f();
	public Vector3f shoulder = new Vector3f();
	
	public Vector3f base = new Vector3f();  // relative to world
	// base orientation, affects entire arm
	public Vector3f base_forward = new Vector3f();
	public Vector3f base_up = new Vector3f();
	public Vector3f base_right = new Vector3f();
	
	// rotating entire robot
	public float base_pan=0;
	public float base_tilt=0;
	
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
}
