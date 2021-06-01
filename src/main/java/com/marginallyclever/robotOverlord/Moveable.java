package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix4d;

public abstract interface Moveable {
	// get the movable thing's current pose
	public abstract void getPoseWorld(Matrix4d m);
	
	// force move to a given pose
	public abstract void setPoseWorld(Matrix4d m);

	/**
	 * Ask this entity "can you move to newWorldPose?"
	 * @param newWorldPose the desired world pose of the subject.
	 * @return true if it can.
	 */
	public abstract boolean canYouMoveTo(Matrix4d newWorldPose);
	
	// TODO if canYouMoveTo says no, get reasons why?
}
