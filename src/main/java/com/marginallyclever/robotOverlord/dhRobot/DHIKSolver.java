package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Matrix4d;

/**
 * Solves Inverse Kinematics for a robot arm.  Assumptions differ with each specific implementation.
 * @author Dan Royer
 */
public abstract class DHIKSolver {
	public static final double EPSILON = 0.00001;

	public static final int NO_SOLUTIONS=0;
	public static final int ONE_SOLUTION=1;
	public static final int MANY_SOLUTIONS=2;

	/**
	 * {@value #solutionFlag} Can be either NO_SOLUTIONS, ONE_SOLUTION, or MANY_SOLUTIONS.
	 */
	public int solutionFlag=DHIKSolver.NO_SOLUTIONS;
	
	
	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public abstract int getSolutionSize();
	
	/**
	 * Starting from a known local origin and a known local hand position ({@DHrobot.endMatrix}), 
	 * calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetMatrix the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 */
	public abstract void solve(DHRobot robot,Matrix4d targetMatrix,DHKeyframe keyframe);
}
