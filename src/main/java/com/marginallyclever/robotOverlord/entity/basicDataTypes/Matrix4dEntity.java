package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.View;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Matrix4dEntity extends AbstractEntity<Matrix4d> {
	Vector3dEntity pos = new Vector3dEntity("Position");
	Vector3dEntity rot = new Vector3dEntity("Rotation");
	
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
		pos.set(trans);
	}
	
	public void getTranslation(Vector3d trans) {
		t.get(trans);
	}
	
	public void setRotation(Matrix3d m) {
		setChanged();
		t.setRotation(m);
		notifyObservers();
		
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
	
	public void getView(View view) {
		view.addVector3(pos);
		view.addVector3(rot);
	}
}
