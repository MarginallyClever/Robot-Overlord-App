package com.marginallyclever.robotOverlord.camera;

import java.util.LinkedList;

import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

/**
 * A point to which the camera can be attached.
 * @author Dan Royer
 *
 */
public class CameraMount extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6256178348242902322L;
	
	static public LinkedList<CameraMount> allMounts = new LinkedList<CameraMount>();
	
	public CameraMount() {
		super();
	}
	
	public void register() {
		allMounts.add(this);
	}
	
	public void unregister() {
		allMounts.remove(this);
	}
}
