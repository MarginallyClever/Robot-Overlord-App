package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix4d;

public abstract interface Moveable {	
	// force move to a given pose
	public abstract void setPoseWorld(Matrix4d m);
	
	// force move to a given pose
	public abstract Matrix4d getPoseWorld();

	/**
	 * Pull a {@link Moveable} object towards a new pose.  It is not guaranteed to
	 * make it all the way.
	 * @param newWorldPose the desired world pose of the subject.
	 */
	public abstract void moveTowards(Matrix4d newWorldPose);
}
