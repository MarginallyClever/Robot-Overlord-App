package com.marginallyclever.robotOverlord.entity.robot.rotaryStewartPlatform;

import java.io.IOException;
import javax.vecmath.Vector3d;

import org.json.JSONObject;

import com.marginallyclever.convenience.JSONSerializable;

public class RotaryStewartPlatformArm implements JSONSerializable {
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


	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void fromJSON(JSONObject arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
}