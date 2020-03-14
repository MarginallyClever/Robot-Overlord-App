package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

public class Matrix4dEntity extends AbstractEntity<Matrix4d> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1066819144032022261L;

	public Matrix4dEntity() {
		super(new Matrix4d());
	}
	
	public Matrix4dEntity(Matrix4d b) {
		super(b);
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
		trans.x = t.m03;
		trans.y = t.m13;
		trans.z = t.m23;
	}
	
	public String toString() {
		return getName()+"="+t.toString();
	}
}
