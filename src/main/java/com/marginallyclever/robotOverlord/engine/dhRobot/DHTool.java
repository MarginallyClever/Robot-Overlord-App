package com.marginallyclever.robotOverlord.engine.dhRobot;

import javax.vecmath.Matrix4d;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool extends DHLink {
	// any child of this tool is either a sub-component of this tool or some world object being held by a gripper.
	
	public DHTool() {
		rangeMin=0;
		rangeMax=0;
		flags = LinkAdjust.NONE;
		refreshPoseMatrix();
		setName("No Tool");
	}
	
	public void set(DHTool b) {
		super.set(b);
		setName(b.getName());
	}
	
	/**
	 * use the keyState to control the tool.
	 * @return true if the robot's pose has been affected.
	 */
	public boolean directDrive() {
		return false;		
	}
	
	public void refreshPose(Matrix4d endMatrix) {
		// update matrix
		refreshPoseMatrix();
		// find cumulative matrix
		endMatrix.mul(getPose());
		poseCumulative.set(endMatrix);

		// set up the physical limits
		cuboid.setPoseWorld(poseCumulative);
		if( getModel() != null ) {
			cuboid.setBounds(getModel().getCuboid().getBoundsTop(), 
							 getModel().getCuboid().getBoundsBottom());
		}
	}

	public String getCommand() {
		return "";
	}
	
	public void sendCommand(String str) {}
	
	public void interpolate(double dt) {}
}
