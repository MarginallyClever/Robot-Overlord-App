package com.marginallyclever.robotOverlord.dhRobot;

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
	 * Starting from a known local origin and a known local hand position (link 6 {@DHrobot.endMatrix}), calculate the angles for the given pose.
	 * @param robot The DHRobot to solve.  Requirements for this robot differ with each solution.
	 */
	public abstract void solve(DHRobot robot);
}
