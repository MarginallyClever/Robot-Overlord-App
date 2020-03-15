package com.marginallyclever.robotOverlord.entity.robotEntity;

import com.jogamp.opengl.GL2;

/**
 * A RobotMotionState is a snapshot of a robot at a moment in time.
 * @author Dan Royer
 *
 */
public interface RobotKeyframe {
	
	/**
	 * Fill this instance with the interpolated value of (b-a)*t+a, where t={0..1}.  
	 *  
	 * @param a The starting at t=0.
	 * @param b The keyframe at t=1.
	 * @param t {0..1}
	 */
	public void interpolate(RobotKeyframe a,RobotKeyframe b,double t);
	
	/**
	 * visualize this keyframe
	 */
	public void render(GL2 gl2);
	
	/**
	 * visualize the change between two keyframes
	 */
	public void renderInterpolation(GL2 gl2,RobotKeyframe arg1);
}
