package com.marginallyclever.robotOverlord.dhRobot;

import com.jogamp.opengl.GL2;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool_GoProCamera extends DHTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public DHTool_GoProCamera() {
		super();
		dhLinkEquivalent.d=80;  // 8cm
		dhLinkEquivalent.r=500;  // 50cm
		dhLinkEquivalent.refreshPoseMatrix();
		setDisplayName("GoPro Camera");
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		// TODO render model file
	}
}
