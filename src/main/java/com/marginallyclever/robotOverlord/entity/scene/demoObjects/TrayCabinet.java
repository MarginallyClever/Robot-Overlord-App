package com.marginallyclever.robotOverlord.entity.scene.demoObjects;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.scene.shape.Shape;

public class TrayCabinet extends Shape {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6205115800152179820L;

	public TrayCabinet() {
		super();
		setName("Cabinet");
		setShapeFilename("/trayCabinet_resized.stl"); 
		getMaterial().setDiffuseColor(1, 1, 1, 1);
		getMaterial().setAmbientColor(1, 1, 1, 1);
		
		Shape tray = new Shape();
		addChild(tray);
		tray.setName("Tray");
		tray.setShapeFilename("/tray_resized.stl");
		tray.setPosition(new Vector3d(3.925*-3,-8.35,0.3+7));
		tray.getMaterial().setDiffuseColor(1, 1, 1, 1);
		tray.getMaterial().setAmbientColor(1, 1, 1, 1);
		
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
	}
}