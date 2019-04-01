package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Point3d;

/**
 * Solves IK for a RTTRTR robot
 * @author Dan Royer
 *
 */
public class DHIKSolveRTTRTR {
	public double theta0;
	public double alpha1;
	public double alpha2;
	// joint 3 is a dummy joint to draw the skeleton correctly.
	public double theta4;
	public double alpha5;
	public double theta6;
	
	public DHIKSolveRTTRTR() {}
	
	public void solve(DHRobot robot) {
		DHLink link0 = robot.links.get(0);
		DHLink link1 = robot.links.get(1);
		DHLink link2 = robot.links.get(2);
		DHLink link3 = robot.links.get(3);
		DHLink link4 = robot.links.get(4);
		DHLink link5 = robot.links.get(5);
		DHLink link6 = robot.links.get(6);

		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		Point3d p2 = new Point3d();
		Point3d p3 = new Point3d();
		Point3d p4 = new Point3d();
		Point3d p5 = new Point3d();
		link0.poseCumulative.transform(new Point3d(0,0,0),p0);
		link1.poseCumulative.transform(new Point3d(0,0,0),p1);
		link2.poseCumulative.transform(new Point3d(0,0,0),p2);
		link3.poseCumulative.transform(new Point3d(0,0,0),p3);
		link4.poseCumulative.transform(new Point3d(0,0,0),p4);
		link5.poseCumulative.transform(new Point3d(0,0,0),p5);
		//System.out.println("p0="+p0+"\tp1="+p1+"\tp2="+p2+"\tp4="+p4+"\tp5="+p5);
		
		//(1) theta0 = tan(x05/y05)
		double x05 = p5.x - p0.x;
		double y05 = p5.y - p0.y;
		theta0 = Math.toDegrees(Math.atan2(x05,-y05));  // TODO explain why this is -y05 instead of y05
		//System.out.println(""t0="+theta0+"\t");
		
		//(2) A = sqrt(x12^2 + y12^2)
		double x12 = p2.x - p1.x;
		double y12 = p2.y - p1.y;
		double A = Math.sqrt(x12*x12 + y12*y12);
		// Correct sign of A with dot product
		double dot = x05*x12 + y05*x12;
		if(dot<0) A=-A;
		
		//(3) B = z12
		double B = p2.z-p1.z;

		//(4)
		alpha1 = Math.toDegrees(Math.atan2(A,B));
		//System.out.println("A="+A+"\t"+"B="+B+"\t"+"a1="+alpha1+"\t");
		
		//(5) C = z23
		double C = p3.z-p2.z; 

		//(6) D = sqrt(x23^2 + y23^2)
		double x23 = p3.x-p2.x;
		double y23 = p3.y-p2.y;
		double D = Math.sqrt(x23*x23 + y23*y23);
		// Correct sign of D
		dot = x05*x23 + y05*x23;
		if(dot<0) D=-D;
		
		//(7) phi1 = atan(C,D)
		double phi1 = Math.toDegrees(Math.atan2(D,C));

		//(8) alpha2 = alpha1 - phi1
		alpha2 = phi1-alpha1;	// TODO why is this backwards?
		//System.out.println("C="+C+"\t"+"D="+D+"\t"+"p1="+phi1+"\t"+"a1="+alpha1+"\t"+"a2="+alpha2+"\t");
		
		System.out.println("t0="+formatDouble(theta0)+"\ta1="+formatDouble(alpha1)+"\ta2="+formatDouble(alpha2)+"\t");
	}
	
	protected String formatDouble(double arg0) {
		//return Float.toString(roundOff(arg0));
		return String.format("%.3f", arg0);
	}
}
