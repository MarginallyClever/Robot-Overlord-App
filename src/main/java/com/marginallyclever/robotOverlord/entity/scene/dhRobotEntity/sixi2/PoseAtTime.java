package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

/**
 * Used by Sixi2Live to average motion over time and estimate forces acting on the robot.
 * @author Dan Royer
 *
 */
public class PoseAtTime<T> {
	public long t;  // ms
	public T p;
	
	public PoseAtTime(T pose,long time) {
		super();
		
		this.p=pose;
		this.t=time;
	}
}
