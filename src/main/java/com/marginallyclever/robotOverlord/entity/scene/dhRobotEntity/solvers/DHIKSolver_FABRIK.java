package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;

/**
 * TODO FABRIK solver (http://www.andreasaristidou.com/publications/papers/FABRIK.pdf)
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DHIKSolver_FABRIK extends DHIKSolver {
	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 6;
	}
	
	/**
	 * 
	 */
	@Override
	public SolutionType solveWithSuggestion(DHRobotModel robot,Matrix4d targetMatrix,final PoseFK keyframe,PoseFK suggestion) {
		return SolutionType.NO_SOLUTIONS;
	}
}
