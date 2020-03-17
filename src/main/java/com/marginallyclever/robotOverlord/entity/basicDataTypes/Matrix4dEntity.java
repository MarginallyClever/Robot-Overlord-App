package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Matrix4dEntity extends AbstractEntity<Matrix4d> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1066819144032022261L;

	public Matrix4dEntity() {
		super(new Matrix4d());
		setName("Pose");
	}
	
	public Matrix4dEntity(Matrix4d b) {
		super(b);
		setName("Pose");
	}
	
	public void setIdentity() {
		setChanged();
		t.setIdentity();
		notifyObservers();
	}
	
	public void setTranslation(Vector3d trans) {
		setChanged();
		t.setTranslation(trans);
		notifyObservers();
	}
	
	public void getTranslation(Vector3d trans) {
		t.get(trans);
	}
	
	public void setRotation(Matrix3d rot) {
		setChanged();
		t.setRotation(rot);
		notifyObservers();
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
}
