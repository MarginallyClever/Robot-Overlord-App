package com.marginallyclever.robotOverlord.entity.robot.spidee;

import java.io.IOException;
import javax.vecmath.Vector3d;

import org.json.simple.JSONObject;

import com.marginallyclever.convenience.JSONSerializable;

public class SpideeLocation implements JSONSerializable {
	  Vector3d up = new Vector3d();
	Vector3d left = new Vector3d();
	Vector3d forward = new Vector3d();
	Vector3d pos = new Vector3d();
	Vector3d relative = new Vector3d();
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
