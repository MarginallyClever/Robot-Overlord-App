package com.marginallyclever.robotOverlord.robots.sixi3;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.MatrixHelper;

/**
 * Given the current pose of the robot, find the approximate jacobian, which
 * describe the relationship between joint velocity and cartesian velocity.
 * @See <a href='https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346'>Robot Academy tutorial</a>
 */
public class ApproximateJacobian {
	static public final double ANGLE_STEP_SIZE_DEGREES=0.001;  // degrees
	RobotArmFK mySixi3;
	
	/**
	 * a 6x6 matrix that will be filled with the jacobian.  
	 * The first three columns are translation component. 
	 * The last three columns are the rotation component.
	 */
	public double [][] jacobian;
	
	
	public ApproximateJacobian(RobotArmFK sixi3) {
		mySixi3 = sixi3;
		
		Matrix4d T = sixi3.getEndEffector();
		RobotArmFK temp = new RobotArmFK();

		int DOF = sixi3.getNumBones();
		jacobian = MatrixHelper.createMatrix(6,DOF);
		
		for(int i=0;i<DOF;++i) {
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			double [] newAngles = sixi3.getAngles();
			newAngles[i]+=ANGLE_STEP_SIZE_DEGREES;
			temp.setAngles(newAngles);
			Matrix4d Tnew = temp.getEndEffector();
			
			// use the finite difference in the two matrixes
			// aka the approximate the rate of change (aka the integral, aka the velocity)
			// in one column of the jacobian matrix at this position.
			Matrix4d dT = new Matrix4d();
			dT.sub(Tnew,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));
			
			jacobian[0][i]=dT.m03;
			jacobian[1][i]=dT.m13;
			jacobian[2][i]=dT.m23;

			// find the rotation part
			// these initialT and initialTd were found in the comments on
			// https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
			// and used to confirm that our skew-symmetric matrix match theirs.
			/*
			double[] initialT = {
					 0,  0   , 1   ,  0.5963,
					 0,  1   , 0   , -0.1501,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			double[] initialTd = {
					 0, -0.01, 1   ,  0.5978,
					 0,  1   , 0.01, -0.1441,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			T.set(initialT);
			Td.set(initialTd);
			dT.sub(Td,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));//*/
			
			//Log.message("T="+T);
			//Log.message("Td="+Td);
			//Log.message("dT="+dT);
			Matrix3d T3 = new Matrix3d(
					T.m00,T.m01,T.m02,
					T.m10,T.m11,T.m12,
					T.m20,T.m21,T.m22);
			//Log.message("R="+R);
			Matrix3d dT3 = new Matrix3d(
					dT.m00,dT.m01,dT.m02,
					dT.m10,dT.m11,dT.m12,
					dT.m20,dT.m21,dT.m22);
			//Log.message("dT3="+dT3);
			Matrix3d skewSymmetric = new Matrix3d();
			
			T3.transpose();  // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(dT3,T3);
			
			//Log.message("SS="+skewSymmetric);
			//[  0 -Wz  Wy]
			//[ Wz   0 -Wx]
			//[-Wy  Wx   0]
			
			jacobian[3][i]=skewSymmetric.m12;
			jacobian[4][i]=skewSymmetric.m20;
			jacobian[5][i]=skewSymmetric.m01;
		}
	}
	
	/**
	 * Use the jacobian to get the cartesian velocity from the joint velocity.
	 * @param jointVelocity joint velocity in degrees.  
	 * @param cartesianVelocity 
	 * 		6 doubles - the XYZ translation and UVW rotation forces on the end effector.
	 *		Will be filled with new values
	 */
	public double [] getCartesianFromJoint(final double [] jointVelocity) {
		// vector-matrix multiplication (y = x^T A)
		double [] cartesianVelocity = new double[6];
		int j,k;
		double sum;
		for(j = 0; j < 6; ++j) {
			sum=0;
			for(k = 0; k < 6; ++k) {
				sum += jacobian[k][j] * Math.toRadians(jointVelocity[k]);
			}
			cartesianVelocity[j] = sum;
		}
		return cartesianVelocity;
	}
	
	// https://stackoverflow.com/a/53028167/1159440
	private double [][] getInverseJacobian() {
		int bones = mySixi3.getNumBones();
		if(bones<6) return getInverseJacobianOverdetermined();
		else if(bones>6) return getInverseJacobianUnderdetermined();
		else return MatrixHelper.invert(jacobian);
	}

	//J_plus = J.transpose * (J*J.transpose()).inverse() // This is for Underdetermined systems
	private double[][] getInverseJacobianUnderdetermined() {
		double [][] jt = MatrixHelper.transpose(jacobian);
		double [][] mm = MatrixHelper.multiplyMatrices(jacobian,jt);
		double [][] ji = MatrixHelper.invert(mm);
		return MatrixHelper.multiplyMatrices(jt,ji);
	}

	//J_plus = (J.transpose()*J).inverse() * J.transpose()
	private double[][] getInverseJacobianOverdetermined() {
		double [][] jt = MatrixHelper.transpose(jacobian);
		double [][] mm = MatrixHelper.multiplyMatrices(jt,jacobian);
		double [][] ji = MatrixHelper.invert(mm);
		return MatrixHelper.multiplyMatrices(ji,jt);
	}

	/**
	 * Use the jacobian to get the joint velocity from the cartesian velocity.
	 * @param cartesianVelocity 6 doubles - the XYZ translation and UVW rotation forces on the end effector.
	 * @return jointVelocity joint velocity in degrees.  Will be filled with the new velocity.
	 * @throws if joint velocities have NaN values
	 */
	public double [] getJointFromCartesian(final double[] cartesianVelocity) throws Exception {
		double [][] inverseJacobian = getInverseJacobian();
		double [] jointVelocity = new double[mySixi3.getNumBones()];
		
		// vector-matrix multiplication (y = x^T A)
		double sum;
		for(int j=0;j<mySixi3.getNumBones();++j) {
			sum=0;
			for(int k=0;k<6;++k) {
				sum += inverseJacobian[j][k] * cartesianVelocity[k];
			}
			if(Double.isNaN(sum)) {
				throw new Exception("Bad inverse Jacobian.  Singularity?");
			}
			jointVelocity[j] = Math.toDegrees(sum);
		}
		
		return jointVelocity;
	}
	
}
