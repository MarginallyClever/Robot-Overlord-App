package com.marginallyclever.ro3.node.nodes.limbsolver.limbsolvers;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.security.InvalidParameterException;

/**
 * Given the current pose of the robot, find the approximate jacobian, which describe the relationship between joint
 * velocity and cartesian velocity.  This version uses screw theory.
 *
 */
public class ApproximateJacobianScrewTheory extends ApproximateJacobian {
	/**
	 * Given the current pose of the robot, find the approximate jacobian.
	 * @param arm the robot to analyze.
	 */
	public ApproximateJacobianScrewTheory(Limb arm) {
		super(arm.getNumJoints());

		Pose parentPose = arm.findParent(Pose.class);
		Matrix4d iBaseWorld = parentPose==null ? MatrixHelper.createIdentityMatrix4() : parentPose.getWorld();
		iBaseWorld.invert();

		var ee = arm.getEndEffector().getSubject();
		if(ee==null) throw new InvalidParameterException("Robot has no end effector.");

		// For each joint
		for (int i = 0; i < DOF; ++i) {
			// get pose of joint i relative to the robot base
			var motor = arm.getJoint(i);
			if(motor==null) throw new InvalidParameterException("joint "+i+" is null.");
			var hinge = motor.getHinge();
			if(hinge==null) throw new InvalidParameterException("motor "+i+" has no hinge.");
			var axle = hinge.getAxle();
			if(axle==null) throw new InvalidParameterException("hinge "+i+" has no axle.");

			Matrix4d jointPose = axle.getWorld();
			jointPose.mul(iBaseWorld);

			// TODO if (hinge.isRevolute)
			{
				// the screw axis is the rotation axis
				Vector3d s = MatrixHelper.getZAxis(jointPose);
				// The angular velocity component of the screw is the same as s
				double[] w = new double[] { s.x,s.y,s.z };

				// Compute the position of the joint origin and end effector
				Vector3d p = MatrixHelper.getPosition(jointPose);  // position of joint origin
				Matrix4d m = ee.getWorld();
				m.mul(iBaseWorld);
				Vector3d p_endEffector = MatrixHelper.getPosition(m); // position of end effector

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
			} // TODO else if(hinge.isPrismatic)
			/*
			{
				// for prismatic joint, the screw axis is the direction of translation
				double[] v = new double[] {T.m02, T.m12, T.m22};
				double[] w = new double[]{0, 0, 0};

				// Fill in the Jacobian column
				for (int j = 0; j < 3; ++j) {
					jacobian[j    ][i] = v[j];
					jacobian[j + 3][i] = w[j];
				}
			}*/
		}
	}
}
