package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.Robot;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Given the current pose of the robot, find the approximate jacobian, which describe the relationship between joint
 * velocity and cartesian velocity.  This version uses screw theory.
 *
 * @since 2.6.1
 * @author Dan Royer
 */
public class ApproximateJacobianScrewTheory extends ApproximateJacobian {
	/**
	 * Given the current pose of the robot, find the approximate jacobian.
	 * @param arm the robot to analyze.
	 */
	public ApproximateJacobianScrewTheory(RobotComponent arm) {
		super(arm.getNumBones());

		int oldActive = (int)arm.get(Robot.ACTIVE_JOINT);
		// For each joint
		for (int i = 0; i < DOF; ++i) {
			// get pose of joint i relative to the robot base
			arm.set(Robot.ACTIVE_JOINT, i);
			Matrix4d T = (Matrix4d) arm.get(Robot.JOINT_POSE);

			// for revolute joint
			if (!(boolean) arm.get(Robot.JOINT_PRISMATIC)) {
				// the screw axis is the rotation axis
				Vector3d s = MatrixHelper.getZAxis(T);
				// The angular velocity component of the screw is the same as s
				double[] w = new double[] { s.x,s.y,s.z };

				// Compute the position of the joint origin and end effector
				Vector3d p = MatrixHelper.getPosition(T);  // position of joint origin
				Vector3d p_endEffector = MatrixHelper.getPosition((Matrix4d)arm.get(Robot.END_EFFECTOR)); // position of end effector

				// Compute the cross product of s and the vector from joint origin to end effector
				Vector3d r = new Vector3d();
				r.sub(p_endEffector, p); // Vector from joint origin to end effector
				Vector3d sXr = new Vector3d();
				sXr.cross(s,r);
				double[] v1 = new double[] { sXr.x,sXr.y,sXr.z };

				// Fill in the Jacobian column
				jacobian[0][i] = v1[0];
				jacobian[1][i] = v1[1];
				jacobian[2][i] = v1[2];
				jacobian[3][i] = w[0];
				jacobian[4][i] = w[1];
				jacobian[5][i] = w[2];
			} else {
				// for prismatic joint, the screw axis is the direction of translation
				double[] v = new double[] {T.m02, T.m12, T.m22};
				double[] w = new double[]{0, 0, 0};

				// Fill in the Jacobian column
				for (int j = 0; j < 3; ++j) {
					jacobian[j    ][i] = v[j];
					jacobian[j + 3][i] = w[j];
				}
			}
		}
		arm.set(Robot.ACTIVE_JOINT, oldActive);
	}
}
