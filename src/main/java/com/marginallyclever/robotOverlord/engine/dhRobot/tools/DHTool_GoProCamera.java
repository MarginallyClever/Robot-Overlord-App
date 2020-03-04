package com.marginallyclever.robotOverlord.engine.dhRobot.tools;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHTool;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool_GoProCamera extends DHTool {
	public DHTool_GoProCamera() {
		super();
		setD(8);  // cm
		flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_ALPHA;
		refreshPoseMatrix();
		setName("GoPro Camera");
		
		setFilename("/Sixi2/gopro/gopro.stl");
		setModelScale(0.1f);
		// adjust the model's position and rotation.
		this.setPosition(new Vector3d(0,0,0.6));
		Matrix3d m = new Matrix3d();
		m.setIdentity();
		m.rotX(Math.toRadians(90));
		Matrix3d m2 = new Matrix3d();
		m2.setIdentity();
		m2.rotY(Math.toRadians(90));
		m.mul(m2);
		this.setRotation(m);
	}
}
