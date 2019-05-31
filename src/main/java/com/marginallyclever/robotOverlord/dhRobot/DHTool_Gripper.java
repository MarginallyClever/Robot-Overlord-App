package com.marginallyclever.robotOverlord.dhRobot;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool_Gripper extends DHTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	PhysicalObject subjectBeingHeld;
	
	public DHTool_Gripper() {
		super();
		dhLinkEquivalent.d=15;  // cm
		dhLinkEquivalent.refreshPoseMatrix();
		setDisplayName("Gripper");
		// setup dhLinkEquivalent
	}
	
	public void render(GL2 gl2) {
		super.render(gl2);
		// TODO render gripper model
	}
}
