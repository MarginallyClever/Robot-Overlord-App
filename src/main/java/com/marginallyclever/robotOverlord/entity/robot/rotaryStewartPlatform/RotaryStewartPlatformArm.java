package com.marginallyclever.robotOverlord.entity.robot.rotaryStewartPlatform;

import javax.vecmath.Vector3d;



public class RotaryStewartPlatformArm {
	public Vector3d shoulder = new Vector3d();
	public Vector3d elbow = new Vector3d();
	public Vector3d shoulderToElbow = new Vector3d();
	public Vector3d wrist = new Vector3d();
	public float angle=0;


	public void set(RotaryStewartPlatformArm other) {
		shoulder.set(other.shoulder);
		elbow.set(other.elbow);
		shoulderToElbow.set(other.shoulderToElbow);
		wrist.set(other.wrist);

		angle = other.angle;
	}
}