package com.marginallyclever.robotoverlord.uiexposedtypes;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Vector3dEntity extends AbstractEntity<Vector3d> {
	public Vector3dEntity() {
		super(new Vector3d());
	}
	
	public Vector3dEntity(String name) {
		super(new Vector3d());
		setName(name);
	}
	
	public Vector3dEntity(String name,Vector3d b) {
		super(b);
		setName(name);
	}
	
	public Vector3dEntity(double x,double y,double z) {
		super(new Vector3d(x,y,z));
	}
	
	public Vector3dEntity(String name,double x,double y,double z) {
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
	
	
	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view. 
	 * @param view the panel to decorate
	 */
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
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
