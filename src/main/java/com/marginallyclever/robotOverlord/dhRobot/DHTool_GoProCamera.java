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
		dhLinkEquivalent.r=50;  // cm
		dhLinkEquivalent.refreshPoseMatrix();
		setDisplayName("GoPro Camera");
		
		setFilename("/gopro.stl");
		setScale(0.1f);	
		this.setPosition(new Vector3d(0,0,dhLinkEquivalent.d));
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.rotY(Math.toRadians(90));
		Matrix4d m2 = new Matrix4d();
		m2.setIdentity();
		m.rotX(Math.toRadians(-90));
		m2.rotZ(Math.toRadians(180));
		m.mul(m2);
		this.setRotation(m);
	}
	
	@Override
	public void render(GL2 gl2) {	
		super.render(gl2);
	}
}
