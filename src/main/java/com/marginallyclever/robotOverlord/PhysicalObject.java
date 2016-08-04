package com.marginallyclever.robotOverlord;


public abstract class PhysicalObject extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1804941485489224976L;


	// set up the future motion state of the physical object
	public void prepareMove(float dt) {}
	
	// apply the future motion state - make the future into the present
	public void finalizeMove() {}
}
