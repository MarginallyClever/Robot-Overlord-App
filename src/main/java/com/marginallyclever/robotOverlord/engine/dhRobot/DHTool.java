package com.marginallyclever.robotOverlord.engine.dhRobot;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool extends DHLink {	
	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	public PhysicalObject subjectBeingHeld;
	
	
	public DHTool() {
		rangeMin=0;
		rangeMax=0;
		flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA |	DHLink.READ_ONLY_R	| DHLink.READ_ONLY_ALPHA;
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

	public String generateGCode() {
		return "";
	}
	
	public void parseGCode(String str) {}
	
	public void interpolate(double dt) {}
}
