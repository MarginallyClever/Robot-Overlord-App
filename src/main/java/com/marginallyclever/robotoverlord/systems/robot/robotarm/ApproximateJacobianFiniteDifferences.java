package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.components.RobotComponent;
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
public class ApproximateJacobianFiniteDifferences implements ApproximateJacobian {

	/**
	 * a matrix that will be filled with the jacobian. The first three columns
	 * are translation component. The last three columns are the rotation component.
	 */
	private double[][] jacobian;
	private int DOF;

	/**
	 * Given the current pose of the robot, find the approximate jacobian.
	 * @param arm the robot to analyze.
	 */
	public ApproximateJacobianFiniteDifferences(RobotComponent arm) {
		DOF = arm.getNumBones();
		jacobian = MatrixHelper.createMatrix(6, DOF);

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

	/**
	 * Use the jacobian to get the cartesian velocity from the joint velocity.
	 * @param jointForce joint velocity in degrees.
	 * @return 6 doubles containing the XYZ translation and UVW rotation forces on the end effector.
	 */
	@Override
	public double[] getCartesianForceFromJointForce(final double[] jointForce) {
		// vector-matrix multiplication (y = x^T A)
		double[] cartesianVelocity = new double[DOF];
		double sum;
		for (int j = 0; j < DOF; ++j) {
			sum = 0;
			for (int k = 0; k < 6; ++k) {
				sum += jacobian[k][j] * Math.toRadians(jointForce[j]);
			}
			cartesianVelocity[j] = sum;
		}
		return cartesianVelocity;
	}

	// https://stackoverflow.com/a/53028167/1159440
	private double[][] getInverseJacobian() {
        if( DOF == 3 ) return getInverseJacobianOverdetermined();

		// old method, Moore-Penrose pseudoinverse
		if (DOF < 6) return getInverseJacobianOverdetermined();
		else if(DOF>=6) return getInverseJacobianUnderdetermined();
		else return MatrixHelper.invert(jacobian);

		// new method
		//return getInverseJacobianDampedLeastSquares(0.0001);
	}

	// J_plus = J.transpose * (J*J.transpose()).inverse() // This is for
	// Underdetermined systems
	private double[][] getInverseJacobianUnderdetermined() {
		double[][] jt = MatrixHelper.transpose(jacobian);
		double[][] mm = MatrixHelper.multiplyMatrices(jacobian, jt);
		double[][] ji = MatrixHelper.invert(mm);
		return MatrixHelper.multiplyMatrices(jt, ji);
	}

	private double[][] getInverseJacobianDampedLeastSquares(double lambda) {
		double[][] jt = MatrixHelper.transpose(jacobian);
		double[][] jjt = MatrixHelper.multiplyMatrices(jacobian, jt);

		// Add lambda^2 * identity matrix to jjt
		for (int i = 0; i < jacobian.length; i++) {
			jjt[i][i] += lambda * lambda;
		}

		double[][] jjt_inv = MatrixHelper.invert(jjt);
		return MatrixHelper.multiplyMatrices(jt, jjt_inv);
	}


	// J_plus = (J.transpose()*J).inverse() * J.transpose()
	private double[][] getInverseJacobianOverdetermined() {
		double[][] jt = MatrixHelper.transpose(jacobian);
		double[][] mm = MatrixHelper.multiplyMatrices(jt, jacobian);
		double[][] ji = MatrixHelper.invert(mm);
		return MatrixHelper.multiplyMatrices(ji, jt);
	}

	/**
	 * Use the Jacobian to get the joint velocity from the cartesian velocity.
	 * @param cartesianVelocity 6 doubles - the XYZ translation and UVW rotation forces on the end effector.
	 * @return jointVelocity joint velocity in degrees. Will be filled with the new velocity.
	 * @throws Exception if joint velocities have NaN values
	 */
	@Override
	public double[] getJointForceFromCartesianForce(final double[] cartesianVelocity) throws Exception {
		double[][] inverseJacobian = getInverseJacobian();
		double[] jointVelocity = new double[DOF];

		// vector-matrix multiplication (y = x^T A)
		for (int j=0; j<DOF; ++j) {
			double sum = 0;
			for (int k=0; k<cartesianVelocity.length; ++k) {
				sum += inverseJacobian[j][k] * cartesianVelocity[k];
			}
			if (Double.isNaN(sum)) {
				throw new Exception("Bad inverse Jacobian.  Singularity?");
			}
			jointVelocity[j] = Math.toDegrees(sum);
		}

		return jointVelocity;
	}

	@Override
	public double[][] getJacobian() {
		return jacobian;
	}
}
