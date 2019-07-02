package com.marginallyclever.robotOverlord.dhRobot;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.modelInWorld.ModelInWorld;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool extends ModelInWorld {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * A DHLink representation of this tool for kinematic solving.
	 */
	DHLink dhLinkEquivalent;
	
	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	PhysicalObject subjectBeingHeld;
	
	/**
	 * Who, if anyone, is holding this tool?
	 */
	DHRobot heldBy;
	
	public DHTool() {
		dhLinkEquivalent = new DHLink();
		dhLinkEquivalent.rangeMin=0;
		dhLinkEquivalent.rangeMax=0;
		dhLinkEquivalent.flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA |	DHLink.READ_ONLY_R	| DHLink.READ_ONLY_ALPHA;
		dhLinkEquivalent.refreshPoseMatrix();
		setDisplayName("No Tool");
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
	}
	
	/**
	 * use the keyState to control the tool.
	 * @param keyState record of human input to drive the tool this frame.
	 * @return true if the robot's pose has been affected.
	 */
	public boolean directDrive(double [] keyState) {
		return false;		
	}
}
