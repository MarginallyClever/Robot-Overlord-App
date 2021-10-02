package com.marginallyclever.robotOverlord.dhRobotEntity.sixi2;

import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;

/**
 * A Sixi2SimSegment describes trapezoidal acceleration and deceleration between two points.
 * Sixi2SimSegments chain together to form a motion profile for a robot.
 * @author aggra
 *
 */
@Deprecated
public class Sixi2SimSegment {
	public PoseFK start;
	public PoseFK end;
	public PoseFK delta;
	public PoseFK normal;
	
	public double start_s;
	public double end_s;
	public double now_s;
	
	public double distance;
	public double nominalSpeed;  // top speed in this segment
	public double entrySpeed;  // per second
	public double exitSpeed;  // per second
	public double acceleration;  // per second per second
	
	public double entrySpeedMax;
	public double accelerateUntilD;  // distance
	public double decelerateAfterD;  // distance

	public double accelerateUntilT;  // seconds
	public double decelerateAfterT;  // seconds
	
	// when optimizing, should we recheck the entry + exit v of this segment?
	public boolean recalculate;
	// is this segment 100% full speed, end to end?
	public boolean nominalLength;
	// is the robot moving through this segment right now?
	public boolean busy;
	
	
	// delta is calculated here in the constructor.
	public Sixi2SimSegment(PoseFK startPose,PoseFK endPose) {
		start  = (PoseFK)startPose.clone();
		end    = (PoseFK)endPose.clone();
		delta  = (PoseFK)endPose.clone();
		normal = (PoseFK)endPose.clone();
		
		distance = 0;
		for(int i=0;i<delta.fkValues.length;++i) {
			double v = endPose.fkValues[i] - startPose.fkValues[i];
			delta.fkValues[i] = v;
			normal.fkValues[i] = v;
			distance += v*v;
		}
		distance = Math.sqrt(distance);
		if(distance>0) {
			for(int i=0;i<normal.fkValues.length;++i) {
				normal.fkValues[i]/=distance;
			}
		}
		busy=false;
		recalculate=true;
	}
}
