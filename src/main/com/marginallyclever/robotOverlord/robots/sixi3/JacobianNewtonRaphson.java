package com.marginallyclever.robotOverlord.robots.sixi3;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.MatrixHelper;

// http://motion.pratt.duke.edu/RoboticSystems/InverseKinematics.html#mjx-eqn-eqNewtonRaphson
public class JacobianNewtonRaphson {
	public static void step(RobotArmIK sixi3) throws Exception {
		Matrix4d m0=sixi3.getEndEffector();
		Matrix4d m1=sixi3.getEndEffectorTarget();
		//System.out.print("m0="+m0);
		//System.out.print("m1="+m1);
		double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(m0, m1);	
		//System.out.println("cartesianDistance="+Arrays.toString(cartesianDistance));
		ApproximateJacobian aj = sixi3.getApproximateJacobian();
		double [] jointDistance=aj.getJointFromCartesian(cartesianDistance);
		double [] angles = sixi3.getAngles();
		for(int i=0;i<angles.length;++i) {
			angles[i]+=jointDistance[i];
		}
		sixi3.setAngles(angles);
	}

	public static void iterate(RobotArmIK sixi3, Matrix4d m4,int tries) throws Exception {
		//System.out.println("JacobianNewtonRaphson.iterate begins");
		RobotArmIK temp = new RobotArmIK();
		temp.setAngles(sixi3.getAngles());
		temp.setEndEffectorTarget(m4);
		while(tries-->=0) {
			JacobianNewtonRaphson.step(temp);
			if(temp.getDistanceToTarget(m4)<0.001) {
				//System.out.println("JacobianNewtonRaphson.iterate hit");
				sixi3.setAngles(temp.getAngles());
				sixi3.setEndEffectorTarget(m4);
				break;
			}
		}
		//System.out.println("JacobianNewtonRaphson.iterate ends ("+tries+")");
	}
}
