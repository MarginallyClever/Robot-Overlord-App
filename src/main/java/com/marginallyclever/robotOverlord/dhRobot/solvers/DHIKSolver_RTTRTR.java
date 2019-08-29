package com.marginallyclever.robotOverlord.dhRobot.solvers;

import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.dhRobot.DHIKSolver;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;

/**
 * Solves Inverse Kinematics for a RTTRTR robot.  It is assumed the first three joints position the end effector
 * and the last three joints orient the end effector.
 * @author Dan Royer
 * @see https://www.youtube.com/watch?v=V_6diIcQl0U
 * @see https://www.youtube.com/watch?v=74tbl9q2_qI
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

	/**
	 * Starting from a known local origin and a known local hand position (link 6 {@DHrobot.endMatrix}), calculate the angles for the given pose.
	 * @param robot The DHRobot description. 
	 * @param targetPose the pose that robot is attempting to reach in this solution.
	 * @param keyframe store the computed solution in keyframe.
	 * @param oldKeyframe a hint about the previous position, to prevent instantaneous flips
	 */
	@SuppressWarnings("unused")
	@Override
	public void solve(DHRobot robot,Matrix4d targetPose,DHKeyframe keyframe) {
		solutionFlag = ONE_SOLUTION;
		
		DHLink link0 = robot.links.get(0);
		DHLink link1 = robot.links.get(1);
		DHLink link2 = robot.links.get(2);
		DHLink link3 = robot.links.get(3);
		DHLink link4 = robot.links.get(4);
		DHLink link5 = robot.links.get(5);
		DHLink link6 = robot.links.get(6);
		DHLink link7 = robot.links.get(7);
		
		Matrix4d targetPoseAdj = new Matrix4d(targetPose);
		
		if(robot.dhTool!=null) {
			// there is a transform between the wrist and the tool tip.
			// use the inverse to calculate the wrist transform.
			robot.dhTool.dhLinkEquivalent.refreshPoseMatrix();
			//Matrix4d inverseToolPose = new Matrix4d(robot.dhTool.dhLinkEquivalent.pose);
			//inverseToolPose.invert();
			//targetPoseAdj.mul(inverseToolPose);

			targetPoseAdj.m03-=targetPoseAdj.m00 * robot.dhTool.dhLinkEquivalent.r;
			targetPoseAdj.m13-=targetPoseAdj.m10 * robot.dhTool.dhLinkEquivalent.r;
			targetPoseAdj.m23-=targetPoseAdj.m20 * robot.dhTool.dhLinkEquivalent.r;

			targetPoseAdj.m03-=targetPoseAdj.m02 * robot.dhTool.dhLinkEquivalent.d;
			targetPoseAdj.m13-=targetPoseAdj.m12 * robot.dhTool.dhLinkEquivalent.d;
			targetPoseAdj.m23-=targetPoseAdj.m22 * robot.dhTool.dhLinkEquivalent.d;
		}
		
		Point3d p7 = new Point3d(
				targetPoseAdj.m03,
				targetPoseAdj.m13,
				targetPoseAdj.m23);
		//p7.sub(robot.getPosition());
		
		Vector3d n7z = new Vector3d(
				targetPoseAdj.m02,
				targetPoseAdj.m12,
				targetPoseAdj.m22);

		// Work backward to get link5 position
		Point3d p5 = new Point3d(n7z);
		assert((link6.flags & DHLink.READ_ONLY_D)!=0);
		p5.scaleAdd(-link6.d,p7);

		// Work forward to get p1 position
		assert((link0.flags & DHLink.READ_ONLY_D)!=0);
		Point3d p1 = new Point3d(0,0,link0.d);

		if(false) {
			Vector3d p5confirm = new Vector3d(
					link5.poseCumulative.m03,
					link5.poseCumulative.m13,
					link5.poseCumulative.m23);
			System.out.println(
					"p7="+p7+"\t"+
					"n7z="+n7z+"\t"+
					"d6="+link6.d+"\t"+
					"p5="+p5+"\t"+
					"p5c="+p5confirm+"\t"+
					"p1="+p1+"\t"
					);
		}
		
		// p5 is at the center of the wrist.  As long as the wrist is not directly on the same z axis as the base
		// I can find the angle around j0 to point at the wrist.
		// (1) theta0 = atan2(y07/x07);
		keyframe.fkValues[0] = Math.toDegrees(Math.atan2(p5.y,p5.x));
		if(false) System.out.println("theta0="+keyframe.fkValues[0]+"\t");
		
		// (2) C=z15
		double z15 = p5.z - p1.z;
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
		assert((link2.flags & DHLink.READ_ONLY_D)!=0);
		assert((link3.flags & DHLink.READ_ONLY_D)!=0);
		assert((link4.flags & DHLink.READ_ONLY_D)!=0);
		double a = link2.d;
		double b2 = link4.d+link5.d;
		double b1 = link3.d;
		double b = Math.sqrt(b2*b2+b1*b1);
		if(false) System.out.println("b="+b+"\t");

		if( e > a+b ) {
			solutionFlag = NO_SOLUTIONS;
			if(false) System.out.println("NO SOLUTIONS (1)");
			return;
		}
		
		double phi = Math.acos( (b*b-a*a-e*e) / (-2.0*a*e) );
		if(false) System.out.println("phi="+Math.toDegrees(phi)+"\t");
		
		// (6) rho = atan2(d,c)
		double rho = Math.atan2(d,z15);
		if(false) System.out.println("rho="+Math.toDegrees(rho)+"\t");
		
		// (7) alpha1 = phi-rho
		keyframe.fkValues[1] = Math.toDegrees(rho-phi);
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
		keyframe.fkValues[2] = Math.toDegrees(phi3 - phi4);
		if(false) System.out.println("alpha2="+keyframe.fkValues[2]+"\t");
		
		// FIRST HALF DONE
		
		// Now to a partial DHRobot.setRobotPose() up to link5.
		// I don't want to alter the original robot so I'll make a deep clone of the robot.links.
		LinkedList<DHLink> clonedLinks = new LinkedList<DHLink>();
		Iterator<DHLink> rli = robot.links.iterator();
		while(rli.hasNext()) {
			DHLink originalLink = rli.next();
			clonedLinks.add(new DHLink(originalLink));  // deep clone
		}

		Matrix4d r04 = new Matrix4d();
		r04.setIdentity();
		
		clonedLinks.get(0).theta = keyframe.fkValues[0];
		clonedLinks.get(1).alpha = keyframe.fkValues[1];
		clonedLinks.get(2).alpha = keyframe.fkValues[2];
		clonedLinks.get(4).theta = 0;

		rli = clonedLinks.iterator();
		int j=0;
		while(rli.hasNext() && j<5) {
			DHLink link = rli.next();
			link.refreshPoseMatrix();  
			link.poseCumulative.set(r04);
			r04.mul(link.pose);
			++j;
		}

		if(false) {
			Vector3d p4original = new Vector3d(
					link4.poseCumulative.m03,
					link4.poseCumulative.m13,
					link4.poseCumulative.m23);
			Vector3d p4cloned = new Vector3d(
					clonedLinks.get(4).poseCumulative.m03,
					clonedLinks.get(4).poseCumulative.m13,
					clonedLinks.get(4).poseCumulative.m23);
			System.out.println("p4o="+p4original);
			System.out.println("p4c="+p4cloned);
		}
		
		// endMatrix is now at j4, but the rotation is unknown.
		Point3d p4 = new Point3d(0,0,0);
		r04.transform(p4);
		
		// test to see if we are near the singularity (when j6-j4=j4.d+j5.d+j6.d)
		double f = link5.d;  // aka z45
		double g = link6.d+link7.d;  // aka z57
		double maximumReach = f+g;
		double h = p4.distance(p7);

		if(false) System.out.println("p7="+p7+"\t");
		if(false) System.out.println("p5="+p5+"\t");
		if(false) System.out.println("p4="+p4+"\t");
		if(false) System.out.println("f="+f+"\t");
		if(false) System.out.println("g="+g+"\t");
		if(false) System.out.println("h="+h+"\t");
		
		if( h-maximumReach > EPSILON ) {
			// out of reach, including rounding error
			solutionFlag = NO_SOLUTIONS;
			if(false) System.out.println("NO SOLUTIONS (2)");
			keyframe.fkValues[3]=
			keyframe.fkValues[4]=
			keyframe.fkValues[5]=0;
			return;
		}
		
		// We have found matrix r04 and we started with r07 (targetPoseAdj).
		// We can get r47 = r04inv * r07 
		r04.setTranslation(new Vector3d(0,0,0));

		Matrix4d r07 = new Matrix4d();
		r07.set(targetPoseAdj);
		r07.setTranslation(new Vector3d(0,0,0));


		Matrix4d r04inv = new Matrix4d();
		try {
			r04inv.invert(r04);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		Matrix4d r47 = new Matrix4d();
		r47.mul(r04inv,r07);
		// sometimes the r47.r22 value was ever so slightly out of range [-1...1]
		if(r47.m22>1) r47.m22=1;
		if(r47.m22<-1) r47.m22=-1;

		if(false) System.out.println("r47="+r47);
		
		// with r47 we can find alpha5
		double a5 = -Math.acos(r47.m22);
		
		if(false) {
			System.out.println(
					"r47.m22="+r47.m22+"\t"+
					"a5="+a5+"\t"+
					"alpha5="+keyframe.fkValues[4]+"\t");
		}
		
		// if (alpha5 % 180) == 0 then we have the singularity.
		double a5copy = a5;
		while(a5copy>= Math.PI) a5copy-=Math.PI;
		while(a5copy<=-Math.PI) a5copy+=Math.PI;
		if(Math.abs(a5copy)<EPSILON) {
			// singularity!
			solutionFlag = MANY_SOLUTIONS;
			if(false) System.out.println("MANY SOLUTIONS");
			keyframe.fkValues[3] = 0;
			double t6 = Math.acos(r47.m00);
			keyframe.fkValues[5] = Math.toDegrees(t6);
			if(false) System.out.println(
					"t0="+StringHelper.formatDouble(keyframe.fkValues[0])+"\t"+
					"a1="+StringHelper.formatDouble(keyframe.fkValues[1])+"\t"+
					"a2="+StringHelper.formatDouble(keyframe.fkValues[2])+"\t"+
					"t4="+StringHelper.formatDouble(keyframe.fkValues[3])+"\t"+
					"a5="+StringHelper.formatDouble(keyframe.fkValues[4])+"\t"+
					"t6="+StringHelper.formatDouble(keyframe.fkValues[5])+"\t");
			return;
		}
		
		// no singularity, so we can continue to solve for theta4 and theta6.
		
		// https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
		double r22=r47.m22;
		
		
		double s5 = Math.sin(a5);
		double t4,t6;
		if(s5>0) {
			a5 = Math.atan2( Math.sqrt(1-r22*r22),r22);
			t4 = Math.atan2(r47.m12,r47.m02);
			t6 = Math.atan2(r47.m21,-r47.m20);
		} else if(s5<0) {
			a5 = Math.atan2(-Math.sqrt(1-r22*r22),r22);
			t4 = Math.atan2(-r47.m12,-r47.m02);
			t6 = Math.atan2(-r47.m21,r47.m20);
		} else {
			// Only the sum of t4+t6 can be found, not the individual angles.
			solutionFlag = NO_SOLUTIONS;
			if(false) System.out.println("NO SOLUTIONS (3)");
			keyframe.fkValues[3]=
			keyframe.fkValues[4]=
			keyframe.fkValues[5]=0;
			return;
		}
		if(false) System.out.println("5="+a5+"\tpos="+a5);

		keyframe.fkValues[3] = Math.toDegrees(t4)-90;
		keyframe.fkValues[4] = -Math.toDegrees(a5);
		keyframe.fkValues[5] = Math.toDegrees(t6)+90;
		
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
	}
}
