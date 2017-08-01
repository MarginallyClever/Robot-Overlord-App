package com.marginallyclever.robotOverlord.deltaRobot3;

import javax.vecmath.Vector3f;

public class DeltaRobot3Arm {
	Vector3f shoulder = new Vector3f();
	Vector3f elbow = new Vector3f();
	Vector3f shoulderToElbow = new Vector3f();
	Vector3f wrist = new Vector3f();

	float angle=0;


	public void set(DeltaRobot3Arm other) {
		shoulder.set(other.shoulder);
		elbow.set(other.elbow);
		shoulderToElbow.set(other.shoulderToElbow);
		wrist.set(other.wrist);

		angle = other.angle;
	}
}