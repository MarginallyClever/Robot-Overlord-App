package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

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
		dhLinkEquivalent.d=8;  // cm
		dhLinkEquivalent.r=0;  // cm
		dhLinkEquivalent.alpha=0;
		dhLinkEquivalent.theta=0;
		dhLinkEquivalent.flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_ALPHA;
		dhLinkEquivalent.refreshPoseMatrix();
		setDisplayName("GoPro Camera");
		
		setFilename("/Sixi2/gopro/gopro.stl");
		setScale(0.1f);
		// adjust the model's position and rotation.
		this.setPosition(new Vector3d(0,0,0.6));
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.rotX(Math.toRadians(90));
		Matrix4d m2 = new Matrix4d();
		m2.setIdentity();
		m2.rotY(Math.toRadians(90));
		m.mul(m2);
		this.setRotation(m);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
	}
}
