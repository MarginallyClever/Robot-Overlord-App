package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import java.beans.PropertyChangeEvent;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Matrix4dEntity extends AbstractEntity<Matrix4d> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4661259130802425144L;
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
		Matrix4d oldValue = new Matrix4d(t);
		t.setIdentity();
		Matrix4d newValue = new Matrix4d(t);
		
		this.notifyPropertyChangeListeners(new PropertyChangeEvent(this,"identity",oldValue,newValue));
	}
	
	public void setTranslation(Vector3d trans) {
		Matrix4d oldValue = new Matrix4d(t);
		t.setTranslation(trans);
		Matrix4d newValue = new Matrix4d(t);
		this.notifyPropertyChangeListeners(new PropertyChangeEvent(this,"position",oldValue,newValue));
		pos.set(trans);
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
	
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	public void getView(ViewPanel view) {
		view.add(pos);
		view.add(rot);
	}
}
