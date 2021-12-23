package com.marginallyclever.robotOverlord.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;

/**
 * Solves Inverse Kinematics for a robot arm.  Assumptions differ with each specific implementation.
 * @author Dan Royer
 */
@Deprecated
public class DHIKSolver {
	public static final double EPSILON = 0.00001;

	public enum SolutionType {
		NO_SOLUTIONS,
		ONE_SOLUTION,
		MANY_SOLUTIONS,
	}
	
	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 1;
	}
	
	public PoseFK createPoseFK() {
		return new PoseFK(getSolutionSize());
	}
	
	/**
	 * Starting from a known local origin and a known local hand position, 
	 * calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetMatrix the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 */
	public SolutionType solve(DHRobotModel robot,final Matrix4d targetMatrix,final PoseFK keyframe) {
		keyframe.fkValues[0]=0;
		// default action do nothing.
		return SolutionType.NO_SOLUTIONS;
	}
	
	/**
	 * Starting from a known local origin and a known local hand position, 
	 * calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetMatrix the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 * @param suggestion suggested values if there is an ambiguity.
	 */
	public SolutionType solveWithSuggestion(DHRobotModel robot,final Matrix4d targetMatrix,final PoseFK keyframe,final PoseFK suggestion) {
		keyframe.fkValues[0]=0;
		// default action do nothing.
		return SolutionType.NO_SOLUTIONS;
	}
}
