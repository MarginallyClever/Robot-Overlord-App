package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool extends DHLink {
	public DHTool() {
		super();
		setName("DHTool");
	}
	
	public void set(DHTool b) {
		super.set(b);
		setName(b.getName());
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			PrimitiveSolids.drawSphere(gl2, 1);
		gl2.glPopMatrix();
	}
	
	/**
	 * use the keyState to control the tool.
	 * @return true if the robot's pose has been affected.
	 */
	public boolean directDrive() {
		return false;		
	}

	public String getCommand() {
		return "";
	}
	
	public void sendCommand(String str) {}
	
	public void interpolate(double dt) {}
}
