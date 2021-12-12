package com.marginallyclever.robotOverlord.robots.robotArm;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;

// http://motion.pratt.duke.edu/RoboticSystems/InverseKinematics.html#mjx-eqn-eqNewtonRaphson
public class JacobianNewtonRaphson {
	public static void step(RobotArmIK arm) throws Exception {
		Matrix4d m0=arm.getEndEffector();
		Matrix4d m1=arm.getEndEffectorTarget();
		//System.out.print("m0="+m0);
		//System.out.print("m1="+m1);
		double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(m0, m1);	
		//System.out.println("cartesianDistance="+Arrays.toString(cartesianDistance));
		ApproximateJacobian aj = arm.getApproximateJacobian();
		double [] jointDistance=aj.getJointFromCartesian(cartesianDistance);
		double [] angles = arm.getAngles();
		for(int i=0;i<angles.length;++i) {
			angles[i]+=jointDistance[i];
		}
		arm.setAngles(angles);
	}

	public static void iterate(RobotArmIK arm, Matrix4d m4,int tries) throws Exception {
		//System.out.println("JacobianNewtonRaphson.iterate begins");
		RobotArmIK temp = (RobotArmIK)arm.clone();
		temp.setAngles(arm.getAngles());
		temp.setEndEffectorTarget(m4);
		for(int i=0;i<tries;++i) {
			JacobianNewtonRaphson.step(temp);
			if(temp.getDistanceToTarget(m4)<0.01) {
				arm.setAngles(temp.getAngles());
				arm.setEndEffectorTarget(m4);
				break;
			}
		}
		double d = temp.getDistanceToTarget(m4);
		System.out.println("JacobianNewtonRaphson.iterate ends ("+d+")");
	}
}
