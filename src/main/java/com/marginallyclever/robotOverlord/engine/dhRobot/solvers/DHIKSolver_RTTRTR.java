package com.marginallyclever.robotOverlord.engine.dhRobot.solvers;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;

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
	public SolutionType solve(DHRobot robot,Matrix4d targetMatrix,DHKeyframe keyframe) {
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
	public SolutionType solveWithSuggestion(DHRobot robot,Matrix4d targetMatrix,DHKeyframe keyframe,DHKeyframe suggestion) {
		DHLink link0 = robot.links.get(0);
		DHLink link1 = robot.links.get(1);
		DHLink link2 = robot.links.get(2);
		DHLink link3 = robot.links.get(3);
		DHLink link4 = robot.links.get(4);
		DHLink link5 = robot.links.get(5);
		DHLink link6 = robot.links.get(6);
		DHLink link7 = robot.links.get(7);

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
		
		Matrix4d link5m = new Matrix4d(link5.getPoseWorld());
		Matrix4d link4m = new Matrix4d(link4.getPoseWorld());
		targetMatrixAdj.mul(iRoot,targetMatrixAdj);
		link5m.mul(iRoot,link5m);
		link4m.mul(iRoot,link4m);
		
		
		Point3d p7 = new Point3d(
				targetMatrixAdj.m03,
				targetMatrixAdj.m13,
				targetMatrixAdj.m23);		
		Vector3d n7z = new Vector3d(
				targetMatrixAdj.m02,
				targetMatrixAdj.m12,
				targetMatrixAdj.m22);

		// Work backward to get link5 position
		Point3d p5 = new Point3d(n7z);
		p5.scaleAdd(-link6.getD(),p7);

		// Work forward to get p1 position
		Point3d p1 = new Point3d(0,0,link0.getD());

		if(false) {
			Vector3d p5confirm = new Vector3d(
					link5m.m03,
					link5m.m13,
					link5m.m23);
			System.out.println(
					"p7="+p7+"\t"+
					"n7z="+n7z+"\t"+
					"d6="+link6.getD()+"\t"+
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
		
		// (2) C=z15
		double z15 = p5.z-p1.z;
		if(false) System.out.println("c="+z15+"\t");
		
		// (3) 
		double x15 = p5.x-p1.x;
		double y15 = p5.y-p1.y;
		double d = Math.sqrt(x15*x15 + y15*y15);
		if(false) System.out.println("d="+d+"\t");
		
		// (4)
		double e = Math.sqrt(z15*z15 + d*d);
		if(false) System.out.println("e="+e+"\t");

		// (5) phi = acos( (b^2 - a^2 - e^2) / (-2*a*e) ) 
		double a = link2.getD();
		double b2 = link4.getD()+link5.getD();
		double b1 = link3.getD();
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
		
		// (7) alpha1 = phi-rho
		keyframe.fkValues[1] = MathHelper.capRotationDegrees(Math.toDegrees(rho-phi),link1.getRangeCenter());
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
		
		// (10) alpha2 - phi3-phi4
		keyframe.fkValues[2] = MathHelper.capRotationDegrees(Math.toDegrees(phi3 - phi4),link2.getRangeCenter());
		if(false) System.out.println("alpha2="+keyframe.fkValues[2]+"\t");
		
		// FIRST HALF DONE
		
		// Now to a partial DHRobot.setRobotPose() up to link5.
		// I don't want to alter the original robot so I'll make a deep clone of the robot.links.

		robot.links.get(0).setTheta(keyframe.fkValues[0]);
		robot.links.get(1).setAlpha(keyframe.fkValues[1]);
		robot.links.get(2).setAlpha(keyframe.fkValues[2]);
		robot.links.get(4).setTheta(0);

		for( DHLink link : robot.links ) {
			link.refreshPoseMatrix();
		}
		Matrix4d r04 = new Matrix4d();
		r04.set(robot.links.get(robot.links.size()-1).getPoseWorld());

		if(false) {
			Vector3d p4original = new Vector3d();
			link4m.get(p4original);
			System.out.println("p4o="+p4original);
			
			Vector3d p4cloned = new Vector3d();
			robot.links.get(4).getPoseWorld().get(p4cloned);
			System.out.println("p4c="+p4cloned);
		}
		
		// endMatrix is now at j4, but the rotation is unknown.
		Point3d p4 = new Point3d(0,0,0);
		r04.transform(p4);
		
		// test to see if we are near the singularity (when j6-j4=j4.d+j5.d+j6.d)
		double f = link5.getD();  // aka z45
		double g = link6.getD()+link7.getD();  // aka z57
		double maximumReach = f+g;
		double h = p4.distance(p7);

		if(false) System.out.println("p7="+p7+"\t");
		if(false) System.out.println("p5="+p5+"\t");
		if(false) System.out.println("p4="+p4+"\t");
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
		
		// We have found matrix r04 and we started with r07 (targetPoseAdj).
		// We can get r47 = r04inv * r07 
		r04.setTranslation(new Vector3d(0,0,0));

		Matrix4d r07 = new Matrix4d();
		r07.set(targetMatrixAdj);
		r07.setTranslation(new Vector3d(0,0,0));

		// r04 is a rotation matrix.  The inverse of a rotation matrix is its transpose.
		Matrix4d r04inv = new Matrix4d(r04);
		r04inv.transpose();
		//Matrix4d r04inv = new Matrix4d();
		//try {
		//	r04inv.invert(r04);
		//}
		//catch(Exception ex) {
		//	ex.printStackTrace();
		//}
		
		Matrix4d r47 = new Matrix4d();
		r47.mul(r04inv,r07);
		// sometimes the r47.r22 value was ever so slightly out of range [-1...1]
		if(r47.m22> 1) r47.m22= 1;
		if(r47.m22<-1) r47.m22=-1;

		if(false) System.out.println("r47="+r47);
		
		// with r47 we can find alpha5
		double a5 = -Math.acos(r47.m22);
		
		if(false) {
			System.out.println(
					"r47.m22="+r47.m22+"\t"+
					"a5="+a5+"\t");
		}
		
		// if (alpha5 % 180) == 0 then we have the singularity.
		double a5copy = a5;
		while(a5copy>= Math.PI) a5copy-=Math.PI;
		while(a5copy<=-Math.PI) a5copy+=Math.PI;
		if(Math.abs(a5copy)<EPSILON) {
			// singularity!
			double t6 = Math.acos(r47.m00);
			keyframe.fkValues[4] = 0;
			keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t6),link5.getRangeCenter());
			if(false) System.out.println(
					"t0="+StringHelper.formatDouble(keyframe.fkValues[0])+"\t"+
					"a1="+StringHelper.formatDouble(keyframe.fkValues[1])+"\t"+
					"a2="+StringHelper.formatDouble(keyframe.fkValues[2])+"\t"+
					"t4="+StringHelper.formatDouble(keyframe.fkValues[3])+"\t"+
					"a5="+StringHelper.formatDouble(keyframe.fkValues[4])+"\t"+
					"t6="+StringHelper.formatDouble(keyframe.fkValues[5])+"\t");
			/*return SolutionType.NO_SOLUTIONS;/*/
			if(suggestion!=null) {
				if(true) System.out.println("ONE OF MANY SOLUTIONS");
				keyframe.fkValues[3] = MathHelper.capRotationDegrees(suggestion.fkValues[3],link3.getRangeCenter());
				return SolutionType.ONE_SOLUTION;
			} else {
				if(true) System.out.println("MANY SOLUTIONS");
				keyframe.fkValues[3] = 0;
				return SolutionType.MANY_SOLUTIONS;
			}//*/
		}
		
		// no singularity, so we can continue to solve for theta4 and theta6.
		
		// https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
		double r22=r47.m22;
		double s5 = Math.sin(a5);
		double t4,t6;
		if(s5>0) {
			//if(true) System.out.println("A");
			a5 = Math.atan2( Math.sqrt(1-r22*r22),r22);
			t4 = Math.atan2(r47.m12,r47.m02);
			t6 = Math.atan2(r47.m21,-r47.m20);
		} else if(s5<0) {
			//if(true) System.out.println("B");
			a5 = Math.atan2(-Math.sqrt(1-r22*r22),r22);
			t4 = Math.atan2(-r47.m12,-r47.m02);
			t6 = Math.atan2(-r47.m21,r47.m20);
		} else {
			// Only the sum of t4+t6 can be found, not the individual angles.
			// this is the same as if(Math.abs(a5copy)<EPSILON) above, so this should
			// be unreachable.
			if(true) System.out.println("NO SOLUTIONS (3)");
			keyframe.fkValues[3]=
			keyframe.fkValues[4]=
			keyframe.fkValues[5]=0;
			return SolutionType.NO_SOLUTIONS;
		}
		
		if(false) System.out.println("5="+a5+"\tpos="+a5);

		keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t4)-90,link3.getRangeCenter());
		keyframe.fkValues[4] = MathHelper.capRotationDegrees(-Math.toDegrees(a5),link4.getRangeCenter());
		keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t6)+90,link5.getRangeCenter());

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
		if(Double.isNaN(keyframe.fkValues[4])) {
			System.out.println("NaN fk4");
		}
		if(false) System.out.println(
				"r47.m20="+StringHelper.formatDouble(r47.m20)+"\t"+
				"t6="+StringHelper.formatDouble(t6)+"\t"+
				"theta6="+StringHelper.formatDouble(keyframe.fkValues[5])+"\t"+
				"Math.sin(a5)="+StringHelper.formatDouble(Math.sin(a5))+"\t"+
				"r47.m12="+StringHelper.formatDouble(r47.m12)+"\t"+
				"t4="+StringHelper.formatDouble(t4)+"\t"+
				"theta4="+StringHelper.formatDouble(keyframe.fkValues[3])+"\t");

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
