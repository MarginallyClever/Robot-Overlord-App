package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Matrix4dEntity extends AbstractEntity<Matrix4d> {
	Vector3dEntity pos = new Vector3dEntity("Position");
	Vector3dEntity rot = new Vector3dEntity("Rotation");
	
	public Matrix4dEntity() {
		super(new Matrix4d());
		setName("Pose");
		pos.addPropertyChangeListener(this);
		rot.addPropertyChangeListener(this);
	}
	
	public Matrix4dEntity(Matrix4d b) {
		super(b);
		setName("Pose");
	}
	
	public void setIdentity() {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		t.set(m);
	}
	
	public void setTranslation(Vector3d trans) {
		pos.set(trans);
		Matrix4d newValue = new Matrix4d(t);
		newValue.setTranslation(trans);
		t.set(newValue);
	}
	
	public void getTranslation(Vector3d trans) {
		t.get(trans);
	}
	
	public void setRotation(Matrix3d m) {
		Matrix4d oldValue = new Matrix4d(t);
		t.setRotation(m);
		Matrix4d newValue = new Matrix4d(t);
		this.notifyPropertyChangeListeners(new PropertyChangeEvent(this,"rotation",oldValue,newValue));
		
		Vector3d r = MatrixHelper.matrixToEuler(t);
		rot.set(Math.toDegrees(r.x),
				Math.toDegrees(r.y),
				Math.toDegrees(r.z));
	}
	
	@Override
	public void set(Matrix4d m) {
		super.set(m);
		
		Vector3d trans = MatrixHelper.getPosition(t);
		pos.set(trans);
		Vector3d r = MatrixHelper.matrixToEuler(t);
		rot.set(Math.toDegrees(r.x),
				Math.toDegrees(r.y),
				Math.toDegrees(r.z));
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		Matrix4d m4 = new Matrix4d();
		Vector3d rDeg = rot.get();
		Vector3d rRad = new Vector3d(
				Math.toRadians(rDeg.x),
				Math.toRadians(rDeg.y),
				Math.toRadians(rDeg.z)); 
		Matrix3d m3 = MatrixHelper.eulerToMatrix(rRad);
		m4.set(m3);
		m4.setTranslation(pos.get());
		this.set(m4);
	}
	
	public void getRotation(Matrix3d rot) {
		t.get(rot);
	}
	
	public void get(Matrix3d m) {
		t.get(m);
	}
	
	public void get(Vector3d v) {
		t.get(v);
	}
	
	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.add(pos);
		view.add(rot);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject jo = super.toJSON();
		double [] list = MatrixHelper.matrixtoArray(get());
		JSONArray array = new JSONArray();
		for(double d : list) array.put(d);
		jo.put("value",array);
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jsonObject) throws JSONException {
		super.parseJSON(jsonObject);
		JSONArray ja = jsonObject.getJSONArray("value");
		double [] list = new double[16];
		for(int i=0;i<list.length;++i) {
			list[i] = ja.getDouble(i);
		}
		set(new Matrix4d(list));
	}
}
