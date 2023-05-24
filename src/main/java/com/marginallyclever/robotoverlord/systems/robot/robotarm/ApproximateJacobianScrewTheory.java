package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.Robot;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Given the current pose of the robot, find the approximate jacobian, which describe the relationship between joint
 * velocity and cartesian velocity.  This version uses screw theory.
 *
 * @since 2.6.1
 * @author Dan Royer
 */
public class ApproximateJacobianScrewTheory implements ApproximateJacobian {
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
	public ApproximateJacobianScrewTheory(RobotComponent arm) {
		DOF = (int) arm.get(Robot.NUM_JOINTS);
		jacobian = new double[6][DOF];

		// For each joint
		for (int i = 0; i < DOF; ++i) {
			// get pose of joint i relative to the robot base
			arm.set(Robot.ACTIVE_JOINT, i);
			Matrix4d T = (Matrix4d) arm.get(Robot.JOINT_POSE);

			// for revolute joint
			if (!(boolean) arm.get(Robot.JOINT_PRISMATIC)) {
				// the screw axis is the rotation axis
				Vector3d s = new Vector3d(T.m02, T.m12, T.m22);  // rotation axis

				// The angular velocity component of the screw is the same as s
				double[] w = new double[] { s.x,s.y,s.z };

				// Compute the position of the joint origin and end effector
				Vector3d p = new Vector3d(T.m03, T.m13, T.m23);  // position of joint origin
				Point3d p_endEffector = (Point3d) arm.get(Robot.END_EFFECTOR_TARGET_POSITION); // position of end effector

				// Compute the cross product of s and the vector from joint origin to end effector
				Vector3d r = new Vector3d();
				r.sub(p_endEffector, p); // Vector from joint origin to end effector
				Vector3d v = new Vector3d();
				v.cross(s,r);
				double[] v1 = new double[] { v.x,v.y,v.z };

				// Fill in the Jacobian column
				for (int j = 0; j < 3; ++j) {
					jacobian[j][i] = v1[j];
					jacobian[j + 3][i] = w[j];
				}
			} else {
				// for prismatic joint, the screw axis is the direction of translation
				double[] v = new double[] {T.m02, T.m12, T.m22};
				double[] w = new double[]{0, 0, 0};

				// Fill in the Jacobian column
				for (int j = 0; j < 3; ++j) {
					jacobian[j][i] = v[j];
					jacobian[j + 3][i] = w[j];
				}
			}
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
