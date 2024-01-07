package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Calculates the approximate jacobian for a robot arm using
 * <a href="https://en.wikipedia.org/wiki/Finite_difference_method">finite differences</a>.
 */
public class ApproximateJacobianFiniteDifferences extends ApproximateJacobian {
    public double ANGLE_STEP_SIZE_DEGREES = 0.1; // degrees
    public ApproximateJacobianFiniteDifferences(Limb arm) {
        super(arm.getNumJoints());

        Pose endEffector = arm.getEndEffector();
        if(endEffector==null) throw new InvalidParameterException("Robot must have an end effector.");

        double[] jointAnglesOriginal = arm.getAllJointAngles();
        Matrix4d endEffectorPose = endEffector.getWorld();
        Matrix4d endEffectorDifference = new Matrix4d();
        Matrix3d endEffectorPoseRotation = new Matrix3d();
        Matrix3d endEffectorDifferenceRotation = new Matrix3d();
        Matrix3d skewSymmetric = new Matrix3d();
        try {
            for (int i = 0; i < DOF; ++i) {
                // use anglesB to get the hand matrix after a tiny adjustment on one joint.
                double[] jointAnglesPlusDelta = Arrays.copyOf(jointAnglesOriginal, jointAnglesOriginal.length);
                jointAnglesPlusDelta[i] += ANGLE_STEP_SIZE_DEGREES;
                arm.setAllJointAngles(jointAnglesPlusDelta);
                Matrix4d endEffectorPosePlusDelta = endEffector.getWorld();

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
        } finally {
            arm.setAllJointAngles(jointAnglesOriginal);
            arm.update(0);
        }
    }
}
