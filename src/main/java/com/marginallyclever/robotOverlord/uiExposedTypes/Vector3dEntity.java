package com.marginallyclever.robotOverlord.uiExposedTypes;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Vector3dEntity extends AbstractEntity<Vector3d> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6504746583968180431L;


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
	 * @param g
	 */
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}
}
