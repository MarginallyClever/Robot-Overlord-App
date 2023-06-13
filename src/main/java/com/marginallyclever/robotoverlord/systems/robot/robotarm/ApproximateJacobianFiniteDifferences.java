package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.robots.Robot;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

/**
 * <p>Given the current pose of the robot, find the approximate jacobian, which
 * describe the relationship between joint velocity and cartesian velocity.  This method uses finite differences.</p>
 * <ul>
 *     <li>Slightly perturb each joint angle.</li>
 *     <li>Compute the new end effector pose after this perturbation.</li>
 *     <li>Subtract the original end effector pose from the perturbed pose to get the difference, which is an approximation of the Jacobian column corresponding to the current joint.</li>
 *     <li>Fill this difference into the Jacobian matrix.</li>
 * </ul>
 *
 * See <a href="https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346">Robot
 *      Academy tutorial</a>
 *
 * @since 2.0.0?
 * @author Dan Royer
 */
public class ApproximateJacobianFiniteDifferences extends ApproximateJacobian {
	public double ANGLE_STEP_SIZE_DEGREES = 0.1; // degrees

	/**
	 * Given the current pose of the robot, find the approximate jacobian.
	 * @param arm the robot to analyze.
	 */
	public ApproximateJacobianFiniteDifferences(RobotComponent arm) {
		super(arm.getNumBones());

		Matrix4d endEffectorPose = (Matrix4d)arm.get(Robot.END_EFFECTOR);
		Entity newCopy = arm.getEntity().deepCopy();
		RobotComponent temp = newCopy.getComponent(RobotComponent.class);
		temp.findBones();

		Matrix4d endEffectorDifference = new Matrix4d();
		Matrix3d endEffectorPoseRotation = new Matrix3d();
		Matrix3d endEffectorDifferenceRotation = new Matrix3d();
		Matrix3d skewSymmetric = new Matrix3d();

		for (int i = 0; i < DOF; ++i) {
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			double[] jointAnglesPlusDelta = arm.getAllJointValues();
			jointAnglesPlusDelta[i] += ANGLE_STEP_SIZE_DEGREES;
			temp.setAllJointValues(jointAnglesPlusDelta);
			Matrix4d endEffectorPosePlusDelta = (Matrix4d)temp.get(Robot.END_EFFECTOR);

			// use the finite difference in the two matrixes
			// aka the approximate the rate of change (aka the integral, aka the velocity)
			// in one column of the jacobian matrix at this position.
			endEffectorDifference.sub(endEffectorPosePlusDelta, endEffectorPose);
			endEffectorDifference.mul(1.0 / Math.toRadians(ANGLE_STEP_SIZE_DEGREES));

			jacobian[0][i] = endEffectorDifference.m03;
			jacobian[1][i] = endEffectorDifference.m13;
			jacobian[2][i] = endEffectorDifference.m23;

			// Find the rotation part.
			endEffectorPose.getRotationScale(endEffectorPoseRotation);
			endEffectorDifference.getRotationScale(endEffectorDifferenceRotation);
			endEffectorPoseRotation.transpose(); // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(endEffectorDifferenceRotation, endEffectorPoseRotation);

			// [ 0 -Wz Wy]
			// [ Wz 0 -Wx]
			// [-Wy Wx 0]

			jacobian[3][i] = skewSymmetric.m12;
			jacobian[4][i] = skewSymmetric.m20;
			jacobian[5][i] = skewSymmetric.m01;
		}
	}
}
