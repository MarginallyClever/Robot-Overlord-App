package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;

/**
 * Solves Inverse Kinematics for a RTTRTR robot.  It is assumed the first three joints position the end effector
 * and the last three joints orient the end effector.
 * @author Dan Royer
 * See https://www.youtube.com/watch?v=V_6diIcQl0U
 * See https://www.youtube.com/watch?v=74tbl9q2_qI
 */
public class DHIKSolver_RTTRTR extends DHIKSolver {
	//public double theta0,alpha1,alpha2;
	// link 3 is a dummy to draw the skeleton correctly.
	//public double theta4,alpha5,theta6;
	// link 7 is the final output that we started with.
	
	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 6;
	}

	@Override
	public SolutionType solve(DHRobotEntity robot,Matrix4d targetMatrix,DHKeyframe keyframe) {
		return solveWithSuggestion(robot,targetMatrix,keyframe,null);
	}
	
	/**
	 * Starting from a known local origin and a known local hand position, calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetMatrix the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 * @param suggestion a hint about the previous position, to prevent instantaneous flips
	 */
	@SuppressWarnings("unused")
	@Override
	public SolutionType solveWithSuggestion(DHRobotEntity robot,Matrix4d targetMatrix,DHKeyframe keyframe,DHKeyframe suggestion) {
		DHLink link0 = robot.links.get(0);
		DHLink link1 = robot.links.get(1);
		DHLink link2 = robot.links.get(2);
		DHLink link3 = robot.links.get(3);
		DHLink link4 = robot.links.get(4);
		DHLink link5 = robot.links.get(5);

		Matrix4d iRoot = new Matrix4d(robot.getParentMatrix());
		try {
			iRoot.invert();
		} catch(SingularMatrixException e) {
			return SolutionType.NO_SOLUTIONS;
		}
		
		Matrix4d targetMatrixAdj = new Matrix4d(targetMatrix);
		
		if(robot.dhTool!=null) {
			// There is a transform between the wrist and the tool tip.
			// use the inverse to calculate the wrist transform.
			robot.dhTool.refreshPoseMatrix();

			// remove R component (x axis)
			targetMatrixAdj.m03-=targetMatrixAdj.m00 * robot.dhTool.getR();
			targetMatrixAdj.m13-=targetMatrixAdj.m10 * robot.dhTool.getR();
			targetMatrixAdj.m23-=targetMatrixAdj.m20 * robot.dhTool.getR();
			// remove D component (z axis)
			targetMatrixAdj.m03-=targetMatrixAdj.m02 * robot.dhTool.getD();
			targetMatrixAdj.m13-=targetMatrixAdj.m12 * robot.dhTool.getD();
			targetMatrixAdj.m23-=targetMatrixAdj.m22 * robot.dhTool.getD();
		}
		
		targetMatrixAdj.mul(iRoot);
		Point3d pEndEffector = new Point3d(
				targetMatrixAdj.m03,
				targetMatrixAdj.m13,
				targetMatrixAdj.m23);		
		Vector3d n5z = new Vector3d(
				targetMatrixAdj.m02,
				targetMatrixAdj.m12,
				targetMatrixAdj.m22);

		// Work backward to get link5 position
		Point3d p5 = new Point3d(n5z);
		p5.scaleAdd(-link5.getD(),pEndEffector);

		// Work forward to get p1 position
		Point3d p1 = new Point3d(0,0,link0.getD());

		if(false) {
			Matrix4d link4m = new Matrix4d(link4.getPoseWorld());
			link4m.mul(iRoot,link4m);
			Vector3d p5confirm = new Vector3d(
					link4m.m03,
					link4m.m13,
					link4m.m23);
			System.out.println(
					"p6="+pEndEffector+"\t"+
					"n6z="+n5z+"\t"+
					"d5="+link5.getD()+"\t"+
					"p5="+p5+"\t"+
					"p5c="+p5confirm+"\t"+
					"p1="+p1+"\t"
					);
		}
		
		// p5 is at the center of the wrist.  As long as the wrist is not directly on the same z axis as the base
		// I can find the angle around j0 to point at the wrist.
		// (1) theta0 = atan2(y07/x07);
		keyframe.fkValues[0] = MathHelper.capRotationDegrees(Math.toDegrees(Math.atan2(p5.y,p5.x)),link0.getRangeCenter());
		if(false) System.out.println("theta0="+keyframe.fkValues[0]+"\t");
		
		// (2) c=z15
		double z15 = p5.z-p1.z;
		if(false) System.out.println("c="+z15+"\t");
		
		// (3) 
		double x15 = p5.x-p1.x;
		double y15 = p5.y-p1.y;
		double d = Math.sqrt(x15*x15 + y15*y15);
		if(false) System.out.println("d="+d+"\t");
		
		// (4) e = The distance from the shoulder to the center of picassobox
		double e = Math.sqrt(z15*z15 + d*d);
		if(false) System.out.println("e="+e+"\t");

		// (5) phi = acos( (b^2 - a^2 - e^2) / (-2*a*e) ) 
		double a = link1.getR();
		double b1 = link2.getR();
		double b2 = link3.getD();
		double b = Math.sqrt(b2*b2+b1*b1);
		if(false) System.out.println("b="+b+"\t");
			
		
		if( e > a+b ) {
			// target matrix impossibly far away
			if(false) System.out.println("NO SOLUTIONS (1)");
			return SolutionType.NO_SOLUTIONS;
		}
		
		double phi = Math.acos( (b*b-a*a-e*e) / (-2.0*a*e) );
		if(false) System.out.println("phi="+Math.toDegrees(phi)+"\t");
		
		// (6) rho = atan2(d,c)
		double rho = Math.atan2(d,z15);
		if(false) System.out.println("rho="+Math.toDegrees(rho)+"\t");
		
		// (7) theta1 = phi-rho
		keyframe.fkValues[1] = MathHelper.capRotationDegrees(Math.toDegrees(rho-phi)-90,link1.getRangeCenter());
		if(false) System.out.println("alpha1="+keyframe.fkValues[1]+"\t");
		
		// (8) omega = acos( (a^2-b^2-e^2) / (-2be) )
		double omega = Math.acos( (a*a-b*b-e*e) / (-2.0*b*e) );
		if(false) System.out.println("omega="+Math.toDegrees(omega)+"\t");
		
		// (9) phi3 = phi + omega
		double phi3 = phi+omega;
		if(false) System.out.println("phi3="+Math.toDegrees(phi3)+"\t");
		
		// angle of triangle j3-j2-j5 is phi4.
		// b2^2 = b*b + b1*b1 - 2*b*b1 * cos(phi4)
		double phi4 = Math.acos( (b2*b2-b1*b1-b*b) / (-2.0*b1*b) );
		if(false) System.out.println("phi4="+Math.toDegrees(phi4)+"\t");
		
		// (10) theta2 - phi3-phi4
		keyframe.fkValues[2] = MathHelper.capRotationDegrees(Math.toDegrees(phi3 - phi4),link2.getRangeCenter());
		if(false) System.out.println("alpha2="+keyframe.fkValues[2]+"\t");
		
		// FIRST HALF DONE
		Matrix4d link3m = link3.getPoseWorld();
		
		// Now to a partial DHRobot.setRobotPose() up to link4.
		link0.setTheta(keyframe.fkValues[0]);
		link1.setTheta(keyframe.fkValues[1]);
		link2.setTheta(keyframe.fkValues[2]);
		link3.setTheta(0);

		for( DHLink link : robot.links ) {
			link.refreshPoseMatrix();
		}
		Matrix4d r03 = link4.getPoseWorld();
		r03.mul(iRoot);

		if(false) {
			link3m.mul(iRoot,link3m);
			Vector3d p3original = new Vector3d();
			link3m.get(p3original);
			
			Vector3d p3cloned = new Vector3d();
			link3.getPoseWorld().get(p3cloned);
			p3cloned.sub(p3original);
			System.out.println("p3d="+p3cloned);
		}
		
		// endMatrix is now at j3, but the rotation is unknown.
		Point3d p3 = new Point3d(r03.m03,r03.m13,r03.m23);
		
		// test to see if we are near the singularity (when hand nearly straight aka maximum reach)
		double h = p3.distance(pEndEffector);
		double f = link3.getD()+link3.getR();
		double g = link5.getD()+link5.getR();
		double maximumReach = f+g;

		if(false) System.out.println("p7="+pEndEffector+"\t");
		if(false) System.out.println("p5="+p5+"\t");
		if(false) System.out.println("p3="+p3+"\t");
		if(false) System.out.println("f="+f+"\t");
		if(false) System.out.println("g="+g+"\t");
		if(false) System.out.println("h="+h+"\t");

		if( h-maximumReach > EPSILON ) {
			// out of reach
			if(false) System.out.println("NO SOLUTIONS (2)");
			keyframe.fkValues[3]=
			keyframe.fkValues[4]=
			keyframe.fkValues[5]=0;
			return SolutionType.NO_SOLUTIONS;
		}
		
		// We have found matrix r03 and we started with r06 (targetPoseAdj).
		// We can get r36 = r03inv * r06 
		r03.setTranslation(new Vector3d(0,0,0));

		Matrix4d r06 = new Matrix4d(targetMatrixAdj);
		r06.setTranslation(new Vector3d(0,0,0));

		// r04 is a rotation matrix.  The inverse of a rotation matrix is its transpose.
		Matrix4d r03inv = new Matrix4d(r03);
		// transpose is the same as inverse in a matrix with no translation.
		r03inv.transpose();

		
		Matrix4d r36 = new Matrix4d();
		r36.mul(r03inv,r06);
		// sometimes the r46.r22 value was ever so slightly out of range [-1...1]

		if(false) System.out.println("r36.m22="+r36.m22);
		
		// with r36 we can find theta4
		double t4 = Math.acos(r36.m22);
		
		if(false) {
			System.out.println(
					"r36.m22="+r36.m22+"\t"+
					"t4="+t4+"\t");
		}
		
		// if (theta4 % 180) == 0 then we have the singularity.
		double t4copy = MathHelper.capRotationRadians(t4,0);
		if(Math.abs(t4copy)<EPSILON) {
			// singularity!
			double t5 = Math.acos(r36.m00);
			keyframe.fkValues[4] = 0;
			keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t5),link5.getRangeCenter());
			if(false) System.out.println(
					"j0="+StringHelper.formatDouble(keyframe.fkValues[0])+"\t"+
					"j1="+StringHelper.formatDouble(keyframe.fkValues[1])+"\t"+
					"j2="+StringHelper.formatDouble(keyframe.fkValues[2])+"\t"+
					"j3="+StringHelper.formatDouble(keyframe.fkValues[3])+"\t"+
					"j4="+StringHelper.formatDouble(keyframe.fkValues[4])+"\t"+
					"j5="+StringHelper.formatDouble(keyframe.fkValues[5])+"\t");
			if(suggestion!=null) {
				if(true) System.out.println("ONE OF MANY SOLUTIONS");
				keyframe.fkValues[3] = MathHelper.capRotationDegrees(suggestion.fkValues[3],link3.getRangeCenter());
				return SolutionType.ONE_SOLUTION;
			} else {
				if(true) System.out.println("MANY SOLUTIONS");
				keyframe.fkValues[3] = 0;
				return SolutionType.MANY_SOLUTIONS;
			}
		}

		// no singularity, so we can continue to solve for theta4 and theta6.
		
		// https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
		double s4 = Math.sin(t4);
		if(Math.abs(s4)<1e-6) {
			// Only the sum of t4+t6 can be found, not the individual angles.
			// this is the same as if(Math.abs(a5copy)<EPSILON) above, so this should
			// be unreachable.
			if(true) System.out.println("NO SOLUTIONS (3)");
			keyframe.fkValues[3]=
			keyframe.fkValues[4]=
			keyframe.fkValues[5]=0;
			return SolutionType.NO_SOLUTIONS;
		}
		
		double r22=r36.m22;
		double t3,t5;
		double t4Pos = Math.atan2( Math.sqrt(1.0-r22*r22),r22);
		double t4Neg = Math.atan2(-Math.sqrt(1.0-r22*r22),r22);
		
		if(s4>0) {
			t3 = Math.atan2(r36.m12, r36.m02);
			t4 = t4Pos;
			t5 = Math.atan2(r36.m21,-r36.m20);
		} else {
			//if(true) System.out.println("B");
			t3 = Math.atan2(-r36.m12,-r36.m02);
			t4 = t4Neg;
			t5 = Math.atan2(-r36.m21, r36.m20);
		}
		
		if(false) System.out.println("5="+t4+"\tpos="+t4);

		keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t3),link3.getRangeCenter());
		keyframe.fkValues[4] = MathHelper.capRotationDegrees(Math.toDegrees(t4),link4.getRangeCenter());
		//keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t5),link5.getRangeCenter());

		/*if(suggestion!=null) {
			if(Math.abs(suggestion.fkValues[3]-keyframe.fkValues[3])>20) {
				// probably a flip in the joint
				System.out.println(
						suggestion.fkValues[3]+"/"+keyframe.fkValues[3]+"\t"+
						suggestion.fkValues[4]+"/"+keyframe.fkValues[4]+"\t"+
						suggestion.fkValues[5]+"/"+keyframe.fkValues[5]
						);
				t4-=Math.PI;
				a5=-a5;
				t6+=Math.PI;
				keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t4)-90,0);
				keyframe.fkValues[4] = MathHelper.capRotationDegrees(-Math.toDegrees(a5),0);
				keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t6)+90,0);
			}
		}*/
		if(false) System.out.println("r36.m20="+StringHelper.formatDouble(r36.m20)+"\t"
									+"t5="+StringHelper.formatDouble(t5)+"\t"
									+"theta5="+StringHelper.formatDouble(keyframe.fkValues[5])+"\t"
									+"Math.sin(t4)="+StringHelper.formatDouble(Math.sin(t4))+"\t"
									+"r36.m12="+StringHelper.formatDouble(r36.m12)+"\t"
									+"t3="+StringHelper.formatDouble(t3)+"\t"
									+"theta3="+StringHelper.formatDouble(keyframe.fkValues[3])+"\t");

		if(false) System.out.println("result={"
					+StringHelper.formatDouble(keyframe.fkValues[0])+","
					+StringHelper.formatDouble(keyframe.fkValues[1])+","
					+StringHelper.formatDouble(keyframe.fkValues[2])+","
					+StringHelper.formatDouble(keyframe.fkValues[3])+","
					+StringHelper.formatDouble(keyframe.fkValues[4])+","
					+StringHelper.formatDouble(keyframe.fkValues[5])+"}\t");
		
		return SolutionType.ONE_SOLUTION;
	}
}
