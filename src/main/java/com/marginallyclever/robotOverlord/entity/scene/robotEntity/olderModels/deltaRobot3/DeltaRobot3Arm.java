package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels.deltaRobot3;

import javax.vecmath.Vector3d;

public class DeltaRobot3Arm {
	Vector3d shoulder = new Vector3d();
	Vector3d elbow = new Vector3d();
	Vector3d shoulderToElbow = new Vector3d();
	Vector3d wrist = new Vector3d();

	double angle=0;


	public void set(DeltaRobot3Arm other) {
		shoulder.set(other.shoulder);
		elbow.set(other.elbow);
		shoulderToElbow.set(other.shoulderToElbow);
		wrist.set(other.wrist);

		angle = other.angle;
	}
}