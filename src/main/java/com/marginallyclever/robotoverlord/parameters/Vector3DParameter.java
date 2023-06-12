package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Vector3d;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Vector3DParameter extends AbstractParameter<Vector3d> {
	public Vector3DParameter(String name, Vector3d value) {
		super(name,value);
	}

	public Vector3DParameter(String name) {
		super(name,new Vector3d());
	}

	public Vector3DParameter() {
		super("Vector3D",new Vector3d());
	}

	public Vector3DParameter(String name, double x, double y, double z) {
		super(name,new Vector3d(x,y,z));
	}
	
	public void set(double x,double y,double z) {
		set(new Vector3d(x,y,z));
	}
	
	@Override
	public String toString() {
		return getName()+"="+ get().toString();
	}
	
	@Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		Vector3d v = get();
		jo.put("x", v.x);
		jo.put("y", v.y);
		jo.put("z", v.z);
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
		super.parseJSON(jo,context);
		Vector3d v = get();
		v.x = jo.getDouble("x");
		v.y = jo.getDouble("y");
		v.z = jo.getDouble("z");
		set(v);
	}
}
