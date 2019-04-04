package com.marginallyclever.robotOverlord.dhRobot;

/**
 * Solves Inverse Kinematics for a robot arm.  Assumptions differ with each specific implementation.
 * @author Dan Royer
 */
public abstract interface DHIKSolver {
	
	/**
	 * Starting from a known local origin and a known local hand position (link 6 {@DHrobot.endMatrix}), calculate the angles for the given pose.
	 * @param robot The DHRobot to solve.  Requirements for this robot differ with each solution.
	 */
	public void solve(DHRobot robot);
}
