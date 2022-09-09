package com.marginallyclever.robotoverlord.dhrobotentity.dhtool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotoverlord.dhrobotentity.DHLink;

import javax.vecmath.Matrix4d;

/**
 * DHTool has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
@Deprecated
public abstract class DHTool extends DHLink implements MementoOriginator {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3140513593165370783L;
	// tool tip convenience used in kinematics
	protected DHLink toolTipOffset = new DHLink();
	
	public DHTool() {
		super();
		setName("DHTool");
		addEntity(toolTipOffset);
	}
	
	public void set(DHTool b) {
		super.set(b);
		setName(b.getName());
		b.toolTipOffset.set(toolTipOffset);
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);
			PrimitiveSolids.drawSphere(gl2, 1);

			toolTipOffset.render(gl2);
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

	public Matrix4d getToolTipOffset() {
		return toolTipOffset.getPose();
	}
}
