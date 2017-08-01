package com.marginallyclever.robotOverlord.rotaryStewartPlatform;

import java.io.Serializable;

import javax.vecmath.Vector3f;

public class RotaryStewartPlatformArm implements Serializable {
	private static final long serialVersionUID = 1L;

	public Vector3f shoulder = new Vector3f();
	public Vector3f elbow = new Vector3f();
	public Vector3f shoulderToElbow = new Vector3f();
	public Vector3f wrist = new Vector3f();
	public float angle=0;


	public void set(RotaryStewartPlatformArm other) {
		shoulder.set(other.shoulder);
		elbow.set(other.elbow);
		shoulderToElbow.set(other.shoulderToElbow);
		wrist.set(other.wrist);

		angle = other.angle;
	}
}