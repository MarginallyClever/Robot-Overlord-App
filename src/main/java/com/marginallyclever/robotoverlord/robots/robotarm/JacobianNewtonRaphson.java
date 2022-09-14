package com.marginallyclever.robotoverlord.robots.robotarm;

import com.marginallyclever.convenience.MatrixHelper;

import javax.vecmath.Matrix4d;

/**
 * Newton Raphson process of moving a robot using approximate Jacobians. See
 * http://motion.pratt.duke.edu/RoboticSystems/InverseKinematics.html#mjx-eqn-eqNewtonRaphson
 * 
 * @author Dan Royer
 *
 */
public class JacobianNewtonRaphson {
	/**
	 * Move a {@link RobotArmIK} towards a target TCP pose. IT is assumed
	 * the travel distance is small.
	 * 
	 * @param arm the subject to be moved.
	 * @param m1 destination TCP pose
	 * @throws Exception an exception may occur if the inverse jacobian cannot be
	 *                   calculated, which may occur if the arm is at a singularity.
	 */
	public static void step(RobotArmIK arm,Matrix4d m1) throws Exception {
		Matrix4d m0 = arm.getEndEffector();
		double[] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(m0, m1);
		// Log.message("cartesianDistance="+Arrays.toString(cartesianDistance));
		ApproximateJacobian aj = arm.getApproximateJacobian();
		double[] jointDistance = aj.getJointFromCartesian(cartesianDistance);
		double[] angles = arm.getAngles();
		for (int i = 0; i < angles.length; ++i) {
			angles[i] += jointDistance[i];
		}
		arm.setAngles(angles);
	}

	/**
	 * Move a {@link RobotArmIK} towards a target end effector pose iteratively
	 * using the Newton Raphson method.  If the method fails to reach the target then the arm will not be altered.
	 * 
	 * @param arm   the {@link RobotArmIK} to move.
	 * @param m4    the desired {@link Matrix4d} pose of the end effector.
	 * @param tries the number of attempts to approach the target pose.
	 * @throws Exception an exception may occur if the inverse jacobian cannot be
	 *                   calculated, which may occur if the arm is at a singularity.
	 */
	public static void iterate(RobotArmIK arm, Matrix4d m4, int tries) throws Exception {
		// Log.message("iterate begins");
		RobotArmIK temp = (RobotArmIK) arm.clone();
		for (int i = 0; i < tries; ++i) {
			JacobianNewtonRaphson.step(temp,m4);
			if (temp.getDistanceToTarget(m4) < 0.01) {
				arm.setAngles(temp.getAngles());
				break;
			}
		}
		//Log.message("iterate ends (" + temp.getDistanceToTarget(m4) + ")");
	}
}
