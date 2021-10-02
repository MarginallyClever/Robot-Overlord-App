package com.marginallyclever.robotOverlord.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;

/**
 * TODO FABRIK solver (http://www.andreasaristidou.com/publications/papers/FABRIK.pdf)
 * @author Dan Royer
 * @since 1.6.0
 *
 */
@Deprecated
public class DHIKSolver_FABRIK extends DHIKSolver {
	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	@Override
	public int getSolutionSize() {
		return 6;
	}
	
	/**
	 * 
	 */
	@Override
	public SolutionType solveWithSuggestion(DHRobotModel robot,final Matrix4d targetMatrix,final PoseFK keyframe,PoseFK suggestion) {
		return SolutionType.NO_SOLUTIONS;
	}
}
