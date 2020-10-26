package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.memento.Memento;

/**
 * @author Dan Royer
 *
 */
public class DHTool_GoProCamera extends DHTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2151214977287978928L;

	public DHTool_GoProCamera() {
		super();
		setName("GoPro Camera");
		flags = LinkAdjust.R;
		
		refreshPoseMatrix();
		
		setShapeFilename("/Sixi2/gopro/gopro.stl");
		setShapeScale(0.1f);
		setShapeOrigin(0, 0, 0.5);
		setShapeRotation(90, 90, 0);
		
		// adjust the shape's position and rotation.
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

	@Override
	public Memento getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setState(Memento arg0) {
		// TODO Auto-generated method stub
		
	}
}
