package com.marginallyclever.robotoverlord;

import javax.vecmath.Matrix4d;

@Deprecated
public interface Moveable {
	// force move to a given pose
	void setPoseWorld(Matrix4d m);
	
	// force move to a given pose
	Matrix4d getPoseWorld();

	/**
	 * Pull a {@link Moveable} object towards a new pose.  It is not guaranteed to
	 * make it all the way.
	 * @param newWorldPose the desired world pose of the subject.
	 */
	void moveTowards(Matrix4d newWorldPose);
}
