package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

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
	private static final long serialVersionUID = -1258343109559729552L;

	public DHTool_GoProCamera() {
		super();
		setName("GoPro Camera");
		flags = LinkAdjust.R;
		
		refreshPoseMatrix();
		
		setModelFilename("/Sixi2/gopro/gopro.stl");
		setModelScale(0.1f);
		setModelOrigin(0, 0, 0.5);
		setModelRotation(90, 90, 0);
		
		// adjust the model's position and rotation.
		this.setPosition(new Vector3d(50,0,50));
		Matrix3d m = new Matrix3d();
		m.setIdentity();
		m.rotX(Math.toRadians(90));
		Matrix3d m2 = new Matrix3d();
		m2.setIdentity();
		m2.rotZ(Math.toRadians(90));
		m.mul(m2);
		this.setRotation(m);
	}
}
