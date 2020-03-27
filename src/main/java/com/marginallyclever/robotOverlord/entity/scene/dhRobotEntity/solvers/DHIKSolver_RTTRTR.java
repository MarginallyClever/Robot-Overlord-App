package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
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
	 * Processes two vectors to find a vector that's projected onto a plane.
	 * @param n The normal of the plane to be projected onto.  Always length 1. 
	 * @param k Vector that is to be projected.
	 * @return k Projected k vector on the plane that's normal to n vector.
	 */
	public Vector3d projOntoPlane(Vector3d n, Vector3d k) {
		Vector3d kNorm = new Vector3d();
		// double dotProd = k.dot(n)/n.lengthSquared();
		// n is always length 1.
		double dotProd = k.dot(n);
		
		// kNorm is projection onto the n vector
		kNorm.scale(dotProd, n);
		// k is a reference.  Don't damage the reference.  instead, make a new Vector3d to hold the result.
		Vector3d kProj = new Vector3d(k);
		kProj.sub(kNorm);	
		
		return kProj;
	}


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
//									linkW		//WorldFixed		- anchor.stl	(DH_frame_Anchor)
		DHLink link0 = robot.links.get(0);		//AnchorRot			- shoulder.stl  (DH_frame_Shoulder)
		DHLink link1 = robot.links.get(1);		//ShoulderRot		- bicep.stl		(DH_frame_Elbow)
		DHLink link2 = robot.links.get(2);		//ElbowRot			- forearm.stl	(DH_frame_Ulna)
		DHLink link3 = robot.links.get(3);		//UlnaRot			- fork.stl		(DH_frame_Wrist)
		DHLink link4 = robot.links.get(4);		//WristRot			- picasso.stl	(DH_frame_Hand)
		DHLink link5 = robot.links.get(5);		//HandRot			- hand.stl		(DH_frame_EndEffector)

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

			// remove R component (x axis) ***Theoretically no longer required***
			targetMatrixAdj.m03-=targetMatrixAdj.m00 * robot.dhTool.getR();
			targetMatrixAdj.m13-=targetMatrixAdj.m10 * robot.dhTool.getR();
			targetMatrixAdj.m23-=targetMatrixAdj.m20 * robot.dhTool.getR();
			// remove D component (z axis)
			targetMatrixAdj.m03-=targetMatrixAdj.m02 * robot.dhTool.getD();
			targetMatrixAdj.m13-=targetMatrixAdj.m12 * robot.dhTool.getD();
			targetMatrixAdj.m23-=targetMatrixAdj.m22 * robot.dhTool.getD();
		}
		
		targetMatrixAdj.mul(iRoot,targetMatrixAdj);
		//p5 = pEndEffector
		Point3d p5 = new Point3d(
				targetMatrixAdj.m03,
				targetMatrixAdj.m13,
				targetMatrixAdj.m23);		
		Vector3d z5 = new Vector3d(
				targetMatrixAdj.m02,
				targetMatrixAdj.m12,
				targetMatrixAdj.m22);

		// Work backward to get link4 origin - wrist center
		// p4 = p5 - z5*link5.getD()
		Point3d p4 = new Point3d(z5);
		p4.scaleAdd(-link5.getD(),p5);

		// Work forward to get p0 position (DH frame shoulder)
		Point3d p0 = new Point3d(0,0,link0.getD());
		
		if(false) {
			Matrix4d link4m = link4.getPoseWorld();
			link4m.mul(iRoot,link4m);
			Vector3d p4confirm = new Vector3d(
					link4m.m03,
					link4m.m13,
					link4m.m23);
			System.out.println(
					"p5(w)="+p5+"\n"+
					"z5(w)="+z5+"\n"+
					"d5(w)="+link5.getD()+"\n"+
					"p4(v)="+p4+"\n"+
					"p4c(v)="+p4confirm+"\n"+
					"p0(x)="+p0+"\t"
					);
		}
		
		// p4 is at the center of the wrist.  As long as the wrist origin is not directly on the 
		// zW axis (DH_frame_Anchor) I can find the angle around yW to point at the wrist.
		// (1) theta0 = atan2(y04/x04);
		double t0rad = Math.atan2(p4.y,p4.x);
		keyframe.fkValues[0] = MathHelper.capRotationDegrees(Math.toDegrees(t0rad),link0.getRangeCenter());
		if(false) System.out.println("theta0="+keyframe.fkValues[0]+"\t");
		
		double x04 = p4.x-p0.x;
		double y04 = p4.y-p0.y;
		double z04 = p4.z-p0.z;
		// d = Distance between shoulder origin (p0) and wrist origin (p4) but only in XY plane.
		double d = Math.sqrt(x04*x04 + y04*y04);
		if(false) System.out.println("d="+d+"\t");
		// (4) e = Distance between shoulder origin (p0) and wrist origin (p4)
		double e = Math.sqrt(x04*x04 + y04*y04 + z04*z04);
		if(false) System.out.println("e="+e+"\t");

		// (5) beta = acos((e2 - a^2 - b^2)/(-2ab))			- cosine rule
		double a  = link1.getR();			//L2		= 35.796
		if(false) System.out.println("a="+a+"\t");
		double b1 = link2.getR();			//L3		= 6.4259
		double b2 = link3.getD();			//L4+L5		= 29.355 + 9.35 = 38.705
		double b = Math.sqrt(b2*b2+b1*b1);  //b should be 39.23479598277529
		if(false) System.out.println("b="+b+"\t");
		// (7a) adjust
		// b1 and b2 are at a right angle to each other.
		// that means we'll have to adjust phi by some compensation.
		double phiCompensate = Math.atan2(b1,b2);
		if(false) System.out.println("phiCompensate="+Math.toDegrees(phiCompensate)+"\t");

		if( e > a+b ) {
			// target matrix impossibly far away
			if(false) System.out.println("NO SOLUTIONS (1)");
			return SolutionType.NO_SOLUTIONS;
		}
		
		// a triangle is formed by sides a, b, and e.
		// see https://www.calculator.net/triangle-calculator.html?vc=&vx=39.234&vy=35.796&va=&vz=57.27797015092277&vb=&angleunits=d&x=90&y=18
		// where our 'e' is their 'c'
		// corner phi is inside elbow (p1?).
		
		
		// proof phi is correct
		double phi = Math.acos((e*e-a*a-b*b)/(-2.0*a*b));
		if(false) System.out.println("phi="+Math.toDegrees(phi)+"\t");
		
		// (6) rho = atan2(c, d)
		double rho = Math.atan2(z04, d);
		if(false) System.out.println("rho="+Math.toDegrees(rho)+"\t");
		
		// (7) beta
		double betaY = b*Math.sin(Math.PI-phi);
		double betaX = b*Math.cos(Math.PI-phi);
		double beta = Math.atan2(betaY, betaX);
		
		if(false) System.out.println("beta="+Math.toDegrees(beta)+"\t");
		//if(true) System.out.println("-b-r="+Math.toDegrees(-beta-rho)+"\t");
		//if(true) System.out.println("-p-r="+Math.toDegrees(-phi-rho)+"\t");
		if(false) System.out.println("beta="+Math.toDegrees(beta+phiCompensate)+"\t");
		
		// (8) theta1
		keyframe.fkValues[1] = MathHelper.capRotationDegrees(Math.toDegrees(-beta-phiCompensate),link1.getRangeCenter());
		if(false) System.out.println("theta1="+keyframe.fkValues[1]+"\t");

		// (9) theta2
		double elbowAngle = Math.atan2(b2, b1);
		keyframe.fkValues[2] = MathHelper.capRotationDegrees(Math.toDegrees(-elbowAngle-phi+Math.PI),link2.getRangeCenter());
		if(false) System.out.println("theta2="+keyframe.fkValues[2]+"\t");
		
		// -----------------------------------------------------------------------------------
		// FIRST HALF DONE ----------------------------------------------------------------------------------------------------------
		// -----------------------------------------------------------------------------------
		
		/*
		 * To find "Theta 4" we need:
		 * - z5		= z vector from desired end-effector frame relative to World Frame
		 * - p5		= origin of desired end-effector frame relative to World Frame
		 * - p4		= origin of wrist center relative to World Frame
		 * - p0		= origin of shoulder frame relative to World Frame
		 * 
		 * Using above known variables we can find the projected vector from frame 4 to end-effector
		 * And achieve the absolute value of theta4
		 */
		
		//	v04 = vector pointing from shoulder origin (p0) to wrist origin (p4)
		//	v46 = vector pointing from wrist origin (p4) to end-effector origin (p5)
		Vector3d v04 = new Vector3d(x04, y04, z04);						
		Vector3d v46 = new Vector3d(p5.x-p0.x, p5.y-p0.y, p5.z-p0.z);	
		v46.sub(v04);
		
		//	v46xy0 		= v46 projected onto x0-y0 plane (z0 normal to plane)
		//	v46xy0yz2 	= v46xy0 projected onto y2-z2 plane (x2 normal to plane)
		Matrix4d link0m = new Matrix4d(link0.getPoseWorld());
		Matrix4d link2m = new Matrix4d(link2.getPoseWorld());
		Vector3d z0 = new Vector3d(link0m.m02, link0m.m12, link0m.m22);
		Vector3d x2 = new Vector3d(link2m.m00, link2m.m10, link2m.m20);
		
		Vector3d v46xy0 = projOntoPlane(z0, v46);
		Vector3d v46xy0yz2 = projOntoPlane(x2, v46xy0);
		
		//t4abs		= absolute value of theta 4 will be solved during a solution check	
		double t4abs = Math.cos( v46xy0yz2.length() / link5.getD() );
		
		//This prevent NaN returns but also tells us that the wrist is at a singularity position.
		if(Math.abs(v46xy0yz2.length() - link5.getD()) < EPSILON) t4abs = 0;
		if(false) System.out.println("t4abs="+Math.toDegrees(t4abs)+"\t");
		
		/*
		 * To find "Theta 3" we need:
		 * - y2		= y vector from ulna frame relative to World Frame
		 * - z2		= z vector from ulna frame relative to World Frame
		 * - z5		= z vector from desired end-effector frame relative to World Frame
		 * 
		 * Using the above known variables we can find t3x and t3y
		 * t3x = angle between projected z5 vector and x2 axis
		 * t3y = angle between projected z5 vector and y2 axis
		 */
		Vector3d y2 = new Vector3d(link2m.m01, link2m.m11, link2m.m21);
		Vector3d z2 = new Vector3d(link2m.m02, link2m.m12, link2m.m22);
		
		//z5xy2		= z5 projected onto z2-y2 plane (z2 normal to plane)
		Vector3d z5xy2 = projOntoPlane(z2, z5);
		z5xy2.normalize();
		
		double t3x = Math.acos(z5xy2.dot(x2) / (z5xy2.length()*x2.length()));
		double t3y = Math.acos(z5xy2.dot(y2) / (z5xy2.length()*y2.length()));
		
		/*
		 * Now that we have t3x, t3y and t4abs, we can come up with multiple solutions
		 * There are 4 quadrants where z5xy2 can lie in x2-y2 plane 
		 * and 1-2 solutions in each quadrants and in each axis
		 * 
		 * 							N
		 * 			Quadrant B		|		Quadrant A
		 * 	W	--------------------O---------------------	 E
		 * 			Quadrant C		|		Quadrant D
		 * 							S
		 * 
		 * O		= denoted for z2 axis coming out of the page
		 * N-axis	= positive x2-axis
		 * W-axis	= positive y2-axis 
		 */
		double t3_s1;		double t3_s2;
		double t4_s1;		double t4_s2;
		
		if(t3x < Math.PI/2) {
			if(t3y > Math.PI/2) {
				//z5xy2 is in Quadrant A
				t3_s1 = -t3x;			t3_s2 = Math.PI-t3x;
				t4_s1 = -t4abs;			t4_s2 = t4abs;
			} else if(t3y < Math.PI/2) {
				//z5xy2 is in Quadrant B
				t3_s1 = t3x;			t3_s2 = t3x-Math.PI;
				t4_s1 = -t4abs;			t4_s2 = t4abs;
			} else {
				// t3x = 0, t3y = 90 -> N-axis, only 1 solution since ulna ranges [-179, 179]
				t3_s1 = t3x;            t3_s2 = t3x;   
				t4_s1 = -t4abs;         t4_s2 = -t4abs;
			}
		} else if(t3x > Math.PI/2) {
			if(t3y < Math.PI/2) {
				//z5xy2 is in Quadrant C
				t3_s1 = t3x;			t3_s2 = t3x-Math.PI;
				t4_s1 = -t4abs;         t4_s2 = t4abs;      
			} else if(t3y > Math.PI/2) {
				//z5xy2 is in Quadrant D
				t3_s1 = -t3x;			t3_s2 = Math.PI-t3x;
				t4_s1 = -t4abs;			t4_s2 = t4abs;
			} else {
				// t3x = 180, t3y = 90 -> S-axis only 1 solution
				t3_s1 = t3x;            t3_s2 = t3x;   
			    t4_s1 = t4abs;          t4_s2 = t4abs;
			}
		} else {
			// t3x = 90
			if (t3y == 0) {
				//W-axis
				t3_s1 = t3x;            t3_s2 = -t3x;   
				t4_s1 = -t4abs;         t4_s2 = t4abs;
			} else {
				//E-axis
				t3_s1 = t3x;            t3_s2 = -t3x;   
				t4_s1 = t4abs;          t4_s2 = -t4abs;
			}
		}
		
		//Between 2 solutions, choose the one that's closest to the previous FK values
		double s1_delta = Math.abs(suggestion.fkValues[3] - Math.toDegrees(t3_s1)) + Math.abs(suggestion.fkValues[4] - Math.toDegrees(t4_s1));
		double s2_delta = Math.abs(suggestion.fkValues[3] - Math.toDegrees(t3_s2)) + Math.abs(suggestion.fkValues[4] - Math.toDegrees(t4_s2));
		
		if(s1_delta < s2_delta) {
			keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t3_s1),link3.getRangeCenter());
			keyframe.fkValues[4] = MathHelper.capRotationDegrees(Math.toDegrees(t4_s1),link3.getRangeCenter());
		} else {
			keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t3_s2),link3.getRangeCenter());
			keyframe.fkValues[4] = MathHelper.capRotationDegrees(Math.toDegrees(t4_s2),link3.getRangeCenter());
		}
		
		/*
		 * Find Theta 5, similar to how we found t3x and t3y
		 */
		Matrix4d link4m = new Matrix4d(link4.getPoseWorld());
		Vector3d x4 = new Vector3d(link4m.m00, link4m.m10, link4m.m11);
		Vector3d y4 = new Vector3d(link4m.m01, link4m.m11, link4m.m21);
		Vector3d x5 = new Vector3d(targetMatrixAdj.m00, targetMatrixAdj.m10, targetMatrixAdj.m11);
		
		double t5x = Math.acos(x5.dot(x4) / (x5.length()*x4.length()));
		double t5y = Math.acos(x5.dot(y4) / (x5.length()*y4.length()));
		
		if (t5y > Math.PI/2) {
			keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(-t5x),link5.getRangeCenter());
		} else if(t5y < Math.PI/2) {
			keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t5x),link5.getRangeCenter());
		} else {
			keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(0),link5.getRangeCenter());
		}
		
		return SolutionType.ONE_SOLUTION;
		
		
//		// Now to a partial DHRobot.setRobotPose() up to link4.
//		link0.setTheta(keyframe.fkValues[0]);
//		link1.setTheta(keyframe.fkValues[1]);
//		link2.setTheta(keyframe.fkValues[2]);
//		link3.setTheta(0);
//
//		for( DHLink link : robot.links ) {
//			link.refreshPoseMatrix();
//		}
//		Matrix4d r03 = new Matrix4d();
//		r03.mul(iRoot,link3.getPoseWorld());
//
//		if(false) {
//			Matrix4d link3m = new Matrix4d(link3.getPoseWorld());
//			link3m.mul(iRoot,link3m);
//			Vector3d p3original = new Vector3d();
//			link3m.get(p3original);
//			System.out.println("p3o="+p3original);
//			
//			Vector3d p3cloned = new Vector3d();
//			link3.getPoseWorld().get(p3cloned);
//			System.out.println("p3c="+p3cloned);
//		}
//		
//		// endMatrix is now at j3, but the rotation is unknown.
//		Point3d p3 = new Point3d(r03.m03,r03.m13,r03.m23);
//		
//		// test to see if we are near the singularity (when hand nearly straight aka maximum reach)
//		double h = p3.distance(pEndEffector);
//		double f = link3.getD()+link3.getR();
//		double g = link5.getD()+link5.getR();
//		double maximumReach = f+g;
//
//		if(false) System.out.println("p7="+pEndEffector+"\t");
//		if(false) System.out.println("p5="+p5+"\t");
//		if(false) System.out.println("p3="+p3+"\t");
//		if(false) System.out.println("f="+f+"\t");
//		if(false) System.out.println("g="+g+"\t");
//		if(false) System.out.println("h="+h+"\t");
//
//		if( h-maximumReach > EPSILON ) {
//			// out of reach
//			if(false) System.out.println("NO SOLUTIONS (2)");
//			keyframe.fkValues[3]=
//			keyframe.fkValues[4]=
//			keyframe.fkValues[5]=0;
//			return SolutionType.NO_SOLUTIONS;
//		}
//		
//		// We have found matrix r03 and we started with r06 (targetPoseAdj).
//		// We can get r36 = r03inv * r06 
//		r03.setTranslation(new Vector3d(0,0,0));
//
//		Matrix4d r06 = new Matrix4d(targetMatrixAdj);
//		r06.setTranslation(new Vector3d(0,0,0));
//
//		// r04 is a rotation matrix.  The inverse of a rotation matrix is its transpose.
//		Matrix4d r03inv = new Matrix4d(r03);
//		// transpose is the same as inverse in a matrix with no translation.
//		r03inv.transpose();
//
//		Matrix4d r36 = new Matrix4d();
//		r36.mul(r03inv,r06);
//
//		//System.out.println("r36.m22="+r36.m22);
//		
//		// with r36 we can find theta4
//		// https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
//		double r22=r36.m22;
//		double s4 = Math.sin(Math.acos(r22));
//		double t3,t4,t5;
//		if(s4>0) {
//			//System.out.println("A");
//			t4 = Math.atan2( Math.sqrt(1.0-r22*r22),r22);
//			t3 = Math.atan2(r36.m12, r36.m02);
//			t5 = Math.atan2(r36.m21,-r36.m20);
//		} else if(s4<0) {
//			//System.out.println("B");
//			t4 = Math.atan2(-Math.sqrt(1.0-r22*r22),r22);
//			t3 = Math.atan2(-r36.m12,-r36.m02);
//			t5 = Math.atan2(-r36.m21, r36.m20);
//		} else {
//			// Only the sum of t4+t6 can be found, not the individual angles.
//			// this is the same as if(Math.abs(a5copy)<EPSILON) above, so this should
//			// be unreachable.
//			if(true) System.out.println("NO SOLUTIONS (3)");
//			keyframe.fkValues[3]=
//			keyframe.fkValues[4]=
//			keyframe.fkValues[5]=0;
//			return SolutionType.NO_SOLUTIONS;
//		}
//
//		if(true) System.out.println("r36.m20="+StringHelper.formatDouble(r36.m20)+"\t"
//									+"s4="+StringHelper.formatDouble(s4)+"\t"
//									+"r36.m12="+StringHelper.formatDouble(r36.m12)+"\t"
//									+"t3="+StringHelper.formatDouble(t3)+"\t"
//									+"t5="+StringHelper.formatDouble(t5)+"\t"
//									+"theta3="+StringHelper.formatDouble(keyframe.fkValues[3])+"\t"
//									+"theta5="+StringHelper.formatDouble(keyframe.fkValues[5])+"\t"
//									);
//
//		//keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t4)-90,link3.getRangeCenter());
//		keyframe.fkValues[4] = MathHelper.capRotationDegrees(Math.toDegrees(t3)-90,link4.getRangeCenter());
//		//keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t5)-90,link5.getRangeCenter());
//
//		/*if(suggestion!=null) {
//			if(Math.abs(suggestion.fkValues[3]-keyframe.fkValues[3])>20) {
//				// probably a flip in the joint
//				System.out.println(
//						suggestion.fkValues[3]+"/"+keyframe.fkValues[3]+"\t"+
//						suggestion.fkValues[4]+"/"+keyframe.fkValues[4]+"\t"+
//						suggestion.fkValues[5]+"/"+keyframe.fkValues[5]
//						);
//				t4-=Math.PI;
//				a5=-a5;
//				t6+=Math.PI;
//				keyframe.fkValues[3] = MathHelper.capRotationDegrees(Math.toDegrees(t4)-90,0);
//				keyframe.fkValues[4] = MathHelper.capRotationDegrees(-Math.toDegrees(a5),0);
//				keyframe.fkValues[5] = MathHelper.capRotationDegrees(Math.toDegrees(t6)+90,0);
//			}
//		}*/
//		if(false) System.out.println("result={"
//					+StringHelper.formatDouble(keyframe.fkValues[0])+","
//					+StringHelper.formatDouble(keyframe.fkValues[1])+","
//					+StringHelper.formatDouble(keyframe.fkValues[2])+","
//					+StringHelper.formatDouble(keyframe.fkValues[3])+","
//					+StringHelper.formatDouble(keyframe.fkValues[4])+","
//					+StringHelper.formatDouble(keyframe.fkValues[5])+"}\t");
//		
//		return SolutionType.ONE_SOLUTION;
	}
}
