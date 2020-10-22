package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.log.Log;

/**
 * Solves IK for a RTT robot
 * @author Dan Royer
 * TODO test and finish
 */
public class DHIKSolver_RTT extends DHIKSolver {
	//public double theta0,alpha1,alpha2;

	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 3;
	}

	/**
	 * Starting from a known local origin and a known local hand position, calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetMatrix the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 */
	@SuppressWarnings("unused")
	@Override
	public SolutionType solve(DHRobotModel robot,final Matrix4d targetMatrix,final PoseFK keyframe) {
		DHLink link0 = robot.getLink(0);
		DHLink link1 = robot.getLink(1);
		DHLink link2 = robot.getLink(2);
		DHLink link3 = robot.getLink(3);
		DHLink link4 = robot.getLink(4);

		Matrix4d targetPoseAdj = new Matrix4d(targetMatrix);
		
		if(robot.getToolIndex()!=-1) {
			// there is a transform between the wrist and the tool tip.
			// use the inverse to calculate the wrist Z axis and wrist position.
			robot.getCurrentTool().refreshPoseMatrix();
			Matrix4d inverseToolPose = new Matrix4d(robot.getCurrentTool().getPose());
			inverseToolPose.invert();
			targetPoseAdj.mul(inverseToolPose);
		}
		
		Vector3d p4 = new Vector3d(
				targetPoseAdj.m03,
				targetPoseAdj.m13,
				targetPoseAdj.m23);

		// Work forward to get p1 position
		Point3d p1 = new Point3d(0,0,link0.getD());
		
		// (1) theta0 = atan(y07/x07);
		keyframe.fkValues[0] = Math.toDegrees(Math.atan2(p4.x,-p4.y));  // TODO explain why this isn't Math.atan2(p7.y,p7.x)
		if(false) Log.message("t0="+keyframe.fkValues[0]+"\t");
		
		// (2) C=z14
		double c = p4.z - p1.z;
		if(false) Log.message("c="+c+"\t");
		
		// (3) 
		double x15 = p4.x-p1.x;
		double y15 = p4.y-p1.y;
		double d = Math.sqrt(x15*x15 + y15*y15);
		if(false) Log.message("d="+d+"\t");
		
		// (4)
		double e = Math.sqrt(c*c + d*d);
		if(false) Log.message("e="+e+"\t");

		// (5) phi = acos( (b^2 - a^2 - e^2) / (-2*a*e) ) 
		double a = link2.getD();
		double b2 = link4.getD();
		double b1 = link3.getD();
		double b = Math.sqrt(b2*b2+b1*b1);
		if(false) Log.message("b="+b+"\t");
		
		double phi = Math.acos( (b*b-a*a-e*e) / (-2*a*e) );
		if(false) Log.message("phi="+Math.toDegrees(phi)+"\t");
		
		// (6) rho = atan2(d,c)
		double rho = Math.atan2(d,c);
		if(false) Log.message("rho="+Math.toDegrees(rho)+"\t");
		
		// (7) alpha1 = phi-rho
		keyframe.fkValues[1] = Math.toDegrees(rho - phi);
		if(false) Log.message("a1="+keyframe.fkValues[1]+"\t");
		
		// (8) omega = acos( (a^2-b^2-e^2) / (-2be) )
		double omega = Math.acos( (a*a-b*b-e*e) / (-2*b*e) );
		if(false) Log.message("omega="+Math.toDegrees(omega)+"\t");

		// (9) phi3 = phi + omega
		double phi3 = phi+omega;
		if(false) Log.message("phi3="+Math.toDegrees(phi3)+"\t");
				
		// angle of triangle j3-j2-j5 is ph4.
		// b2^2 = b^+b1^2-2*b*b1*cos(phi4)
		double phi4 = Math.acos( (b2*b2-b1*b1-b*b) / (-2*b1*b) );
		if(false) Log.message("phi4="+Math.toDegrees(phi4)+"\t");
		
		// (10) alpha2 - phi3-phi4
		keyframe.fkValues[2] = Math.toDegrees(phi3 - phi4);
		if(false) Log.message("alpha2="+keyframe.fkValues[2]+"\t");
		
		if(true) Log.message("solution={"+keyframe.fkValues[0]+","+keyframe.fkValues[1]+","+keyframe.fkValues[2]+"}");
		
		return SolutionType.ONE_SOLUTION;
	}
}
