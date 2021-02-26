package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;

/**
 * Solves Inverse Kinematics for a cylindrical, serially-linked robot like the FANUC GMF M-100 robot arm.
 * @author Dan Royer
 * See http://www.robotix.co.uk/products/fanuc/robot/m_series/m100.htm
 */
public class DHIKSolver_Cylindrical extends DHIKSolver {
	//public double theta0,d1,d2,theta3;

	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 4;
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
		DHLink link4 = robot.getLink(robot.getNumLinks()-1);

		Matrix4d targetPoseAdj = new Matrix4d(targetMatrix);
		
		if(robot.getToolIndex()!=-1) {
			// there is a transform between the wrist and the tool tip.
			// use the inverse to calculate the wrist Z axis and wrist position.
			robot.getCurrentTool().refreshDHMatrix();
			Matrix4d inverseToolPose = new Matrix4d(robot.getCurrentTool().getPose());
			inverseToolPose.invert();
			targetPoseAdj.mul(inverseToolPose);
		}
		
		Matrix4d m4 = new Matrix4d(targetPoseAdj);
		
		Point3d p4 = new Point3d(m4.m03,m4.m13,m4.m23);
		
		// the the base rotation
		keyframe.fkValues[0]=Math.toDegrees(Math.atan2(p4.x,-p4.y));

		// the height
		keyframe.fkValues[1]=p4.z;
		
		// the distance out from the center
		keyframe.fkValues[2] = Math.sqrt(p4.x*p4.x + p4.y*p4.y);
		
		// the rotation at the end effector
		Vector4d relativeX = new Vector4d(p4.x,p4.y,0,0);
		relativeX.scale(1/keyframe.fkValues[2]);  // normalize it
		
		Vector4d relativeY = new Vector4d(-relativeX.y,relativeX.x,0,0);

		Vector4d m4x = new Vector4d();
		m4.getColumn(1, m4x);
		
		double rX = m4x.dot(relativeX);
		double rY = m4x.dot(relativeY);
		
		keyframe.fkValues[3] = Math.toDegrees(-Math.atan2(rY,rX));
		
		if(false) {
			Log.message("solution={"+StringHelper.formatDouble(keyframe.fkValues[0])+","+
								keyframe.fkValues[1]+","+
								keyframe.fkValues[2]+","+
								StringHelper.formatDouble(keyframe.fkValues[3])+"}");
		}
		return SolutionType.ONE_SOLUTION;
	}
}
