package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Vector3d;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Vector3DParameter extends AbstractParameter<Vector3d> {
	public Vector3DParameter() {
		super(new Vector3d());
	}
	
	public Vector3DParameter(String name) {
		super(new Vector3d());
		setName(name);
	}
	
	public Vector3DParameter(String name, Vector3d b) {
		super(b);
		setName(name);
	}
	
	public Vector3DParameter(double x, double y, double z) {
		super(new Vector3d(x,y,z));
	}
	
	public Vector3DParameter(String name, double x, double y, double z) {
		super(new Vector3d(x,y,z));
		setName(name);
	}
	
	public void set(double x,double y,double z) {
		t.set(x,y,z);
	}
	
	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject jo = super.toJSON();
		Vector3d v = get();
		jo.put("x", v.x);
		jo.put("y", v.y);
		jo.put("z", v.z);
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo) throws JSONException {
		super.parseJSON(jo);
		Vector3d v = get();
		v.x = jo.getDouble("x");
		v.y = jo.getDouble("y");
		v.z = jo.getDouble("z");
		set(v);
	}
}
