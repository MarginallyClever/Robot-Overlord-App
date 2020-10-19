package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;

/**
 * Used by Sixi2Live to average motion over time and estimate forces acting on the robot.
 * @author Dan Royer
 *
 */
public class PoseAtTime {
	public long t;  // ms
	public PoseFK p;
	
	public PoseAtTime(PoseFK pose,long time) {
		super();
		
		this.p=pose;
		this.t=time;
	}
}
