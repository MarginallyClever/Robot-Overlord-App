package com.marginallyclever.robotOverlord.dhRobotEntity.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink.LinkAdjust;

@Deprecated
public class JacobianHelper {
	// step size for approximate jacobians
	final public static double ANGLE_STEP_SIZE_DEGREES=0.5;
	
	/**
	 * 
	 * @param mStart matrix of start pose
	 * @param mEnd matrix of end pose
	 * @param dt time scale, seconds
	 * @param cartesianForce 6 doubles that will be filled with the XYZ translation and UVW rotation.
	 * @return true if successful
	 */
	static public boolean getCartesianForceBetweenTwoPoses(final Matrix4d mStart,final Matrix4d mEnd,double dt,double[] cartesianForce) {
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d dp = new Vector3d();
		mStart.get(p0);
		mEnd.get(p1);
		dp.sub(p1,p0);
		dp.scale(1.0/dt);

		mStart.setTranslation(new Vector3d(0,0,0));
		mEnd.setTranslation(new Vector3d(0,0,0));
		// get the rotation force
		Quat4d q0 = new Quat4d();
		Quat4d q1 = new Quat4d();
		Quat4d dq = new Quat4d();
		q0.set(mStart);
		q1.set(mEnd);
		dq.sub(q1,q0);
		dq.scale(2/dt);
		Quat4d w = new Quat4d();
		w.mulInverse(dq,q0);
		
		cartesianForce[0]=dp.x;
		cartesianForce[1]=dp.y;
		cartesianForce[2]=dp.z;
		cartesianForce[3]=-w.x;
		cartesianForce[4]=-w.y;
		cartesianForce[5]=-w.z;
		
		return true;
	}
	
	/**
	 * 
	 * @param model a robot model set to the current pose.
	 * @param cartesianForce the XYZ translation and UVW rotation forces on the end effector
	 * @param jvot joint velocity over time, in degrees.  Will be filled with the new velocity
	 * @return false if joint velocities have NaN values
	 */
	static public boolean getJointVelocityFromCartesianForce(DHRobotModel model,final double[] cartesianForce,double [] jvot) {
		// jvot = joint velocity over time
		
		double[][] jacobian = JacobianHelper.approximateJacobian(model);
		double[][] inverseJacobian = MatrixHelper.invert(jacobian);

		int j,k;
		
		for(j = 0; j < 6; ++j) {
			double sum=0;
			for(k = 0; k < 6; ++k) {
				sum += inverseJacobian[k][j] * cartesianForce[k];
			}
			if(Double.isNaN(jvot[j])) {
				for(k = 0; k < 6; ++k) jvot[k]=0;
				return false;
			}
			sum=MathHelper.wrapRadians(sum);
			jvot[j]=Math.toDegrees(sum);
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param model a robot model set to the current pose.
	 * @param jointVelocity from which to calculate the cartesian force.
	 * @return cartesian force calculated
	 */
	static public double [] getCartesianVelocityFromJointVelocity(DHRobotModel model,final double [] jointVelocity) {
		double [] cf = new double[6];  // cartesian force calculated
		double[][] jacobian = JacobianHelper.approximateJacobian(model);

		for( int k=0;k<6;++k ) {
			for( int j=0;j<6;++j ) {
				cf[j] += jacobian[k][j] * jointVelocity[k];
			}
		}
		return cf;
	}
	
	/**
	 * Use Forward Kinematics to approximate the Jacobian matrix for Sixi.
	 * See also https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
	 * @param model a robot model set to the current pose.
	 * @return the approximate jacobian, a 6x6 matrix.
	 */
	static public double [][] approximateJacobian(DHRobotModel model) {
		double [][] jacobian = new double[6][6];
		
		PoseFK oldPose = model.getPoseFK();
		
		Matrix4d T = model.getPoseIK();
		
		PoseFK newPoseFK = model.createPoseFK();
		
		// for all adjustable joints
		for( int i=0;i<model.getNumLinks();++i ) {
			DHLink link = model.getLink(i);
			if(link.flags == LinkAdjust.NONE) continue;
			
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			newPoseFK.set(oldPose);
			newPoseFK.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;
			model.setPoseFK(newPoseFK);
			
			// Tnew will be different from T because of the changes in setPoseFK().
			Matrix4d Tnew = model.getPoseIK();
			
			// use the finite difference in the two matrixes
			// aka the approximate the rate of change (aka the integral, aka the velocity)
			// in one column of the jacobian matrix at this position.
			Matrix4d dT = new Matrix4d();
			dT.sub(Tnew,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));
			
			jacobian[i][0]=dT.m03;
			jacobian[i][1]=dT.m13;
			jacobian[i][2]=dT.m23;

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
			
			jacobian[i][3]=skewSymmetric.m12;
			jacobian[i][4]=skewSymmetric.m20;
			jacobian[i][5]=skewSymmetric.m01;
		}
		
		model.setPoseFK(oldPose);
		
		return jacobian;
	}
}
