package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * DHKeyframe contains the time, IK end effector, and FK values for a DHRobot in a given pose.
 * Linked together DHKeyframes describe animation between poses.
 * @author Dan Royer
 *
 */
public class DHKeyframe implements RobotKeyframe {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public double time;
	
	public Matrix4d poseIK;
	
	public double [] fkValues;
	

	public DHKeyframe(int size) {
		fkValues=new double[size];
	}
	
	public DHKeyframe() {}
	
	@Override
	public void interpolate(RobotKeyframe a, RobotKeyframe b, double t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GL2 gl2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void renderInterpolation(GL2 gl2, RobotKeyframe arg1) {
		// TODO Auto-generated method stub
		
	}
}
