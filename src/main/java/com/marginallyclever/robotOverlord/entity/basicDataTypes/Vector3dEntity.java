package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Vector3dEntity extends AbstractEntity<Vector3d> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8102251678893624449L;

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
	
	public String toString() {
		return getName()+"="+t.toString();
	}
}
