package com.marginallyclever.robotoverlord.robots.skycam;

import javax.vecmath.Vector3d;

@Deprecated
public class SkycamSimSegment {
	public Vector3d start;
	public Vector3d end;
	public Vector3d delta;
	public Vector3d normal;
	
	public double start_s;
	public double end_s;
	public double now_s;
	
	public double distance;
	public double nominalSpeed;  // top speed in this segment
	public double entrySpeed;  // per second
	public double acceleration;  // per second per second

	public double accelerateUntilT;  // seconds
	public double decelerateAfterT;  // seconds
	
	// when optimizing, should we recheck the entry + exit v of this segment?
	public boolean recalculate;

	// is the robot moving through this segment right now?
	public boolean busy;
	
	
	// delta is calculated here in the constructor.
	public SkycamSimSegment(Vector3d startPose,Vector3d endPose) {
		start  = (Vector3d)startPose.clone();
		end    = (Vector3d)endPose.clone();
		delta  = (Vector3d)endPose.clone();
		normal = (Vector3d)endPose.clone();
		
		delta.sub(endPose,startPose);
		normal.set(delta);
		normal.normalize();
		distance = delta.length();
	
		busy=false;
		recalculate=true;
	}
}
