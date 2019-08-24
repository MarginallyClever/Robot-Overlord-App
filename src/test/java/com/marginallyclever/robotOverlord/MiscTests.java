package com.marginallyclever.robotOverlord;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.junit.Test;

import com.marginallyclever.convenience.MatrixOperations;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.dhRobot.DHIKSolver;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.robots.DHRobot_Sixi2;

public class MiscTests {
	@Test
    public void testCompatibleFonts() {
        String s = "\u23EF";
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        System.out.println("Total fonts: \t" + fonts.length);
        int count = 0;
        for (Font font : fonts) {
            if (font.canDisplayUpTo(s) < 0) {
                count++;
                System.out.println(font.getName());
            }
        }
        System.out.println("Compatible fonts: \t" + count);
    }
	
	/**
	 * @see https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
	 */
	@Test
	public void solveLinearEquations() {
		// we have 6 linear equation and six unknowns
		// p0 = a + b*t0 +  c*t0^2 +  d*t0^3 +  e*t0^4 +   f*t0^5
		// pf = a + b*tf +  c*tf^2 +  d*tf^3 +  e*tf^4 +   f*tf^5
		// v0 =     b    + 2c*t0   + 3d*t0^2 + 4e*t0^3 +  5f*t0^4
		// vf =     b    + 2c*tf   + 3d*tf^2 + 4e*tf^3 +  5f*tf^4
		// a0 =          + 2c      + 6d*t0   + 12e*t0^2 + 20f*t0^3
		// af =          + 2c      + 6d*tf   + 12e*tf^2 + 20f*tf^3
		// or expressed as a matrix, Q = M*N
		// [p0]=[ 1 t0  t0^2  t0^3   t0^4   t0^5][a]
		// [pf]=[ 1 tf  tf^2  tf^3   tf^4   tf^5][b]
		// [v0]=[ 0  1 2t0   3t0^2  4t0^3  5t0^4][c]
		// [vf]=[ 0  1 2tf   3tf^2  4tf^3  5tf^4][d]
		// [a0]=[ 0  0 2     6t0   12t0^2 20t0^3][e]
		// [af]=[ 0  0 2     6tf   12tf^2 20tf^3][f]
		// I know Q and M.  I can Q * inverse(M) to get N.
		// then I can solve the original polynomials for any t betwen t0 and tf.
		
		double t0=0,tf=100;
		double p0=0,pf=90;
		double v0=0,vf=0;
		double a0=0,af=0;

		double[] q = new double[6];
		q[0]=p0;
		q[1]=pf;
		q[2]=v0;
		q[3]=vf;
		q[4]=a0;
		q[5]=af;
		
		long start = System.currentTimeMillis();
		
		double[][] m = buildMatrix(t0,tf);
		double[][] mInv=MatrixOperations.invertMatrix(m);
		double[] n = MatrixOperations.multiply(mInv,q);

		long end = System.currentTimeMillis();
		
		double a=n[0];
		double b=n[1];
		double c=n[2];
		double d=n[3];
		double e=n[4];
		double f=n[5];

		System.out.println("time="+(end-start)+"ms");
		//MatrixOperations.printMatrix(m, 1);
		//MatrixOperations.printMatrix(mInv, 1);
		System.out.println("t\tp\tv\ta\t"+a+"\t"+b+"\t"+c+"\t"+d+"\t"+e+"\t"+f);
		for(double t=t0;t<=tf;t++) {
			// p0 = a + b*t0 +  c*t0^2 +  d*t0^3 +   e*t0^4 +   f*t0^5
			// v0 =     b    + 2c*t0   + 3d*t0^2 +  4e*t0^3 +  5f*t0^4
			// a0 =          + 2c      + 6d*t0   + 12e*t0^2 + 20f*t0^3
			double t2=t*t;
			double t3=t*t*t;
			double t4=t*t*t*t;
			double t5=t*t*t*t*t;
			double pt = a * b*t +   c*t2 +   d*t3 +    e*t4 +    f*t5;
			double vt =     b   + 2*c*t  + 3*d*t2 +  4*e*t3 +  5*f*t4;
			double at =         + 2*c    + 6*d*t  + 12*e*t2 + 20*f*t3;
			System.out.println(t+"\t"+pt+"\t"+vt+"\t"+at);
		}
	}
	
	public double[][] buildMatrix(double t0,double tf) {
		double t02 = t0*t0;
		double tf2 = tf*tf;
		double t03 = t02*t0;
		double tf3 = tf2*tf;
		double t04 = t03*t0;
		double tf4 = tf3*tf;
		double t05 = t04*t0;
		double tf5 = tf4*tf;

		double [][] matrix = new double[6][6];

		// [p0]=[ 1 t0  t0^2  t0^3   t0^4   t0^5][a]
		// [pf]=[ 1 tf  tf^2  tf^3   tf^4   tf^5][b]
		// [v0]=[ 0  1 2t0   3t0^2  4t0^3  5t0^4][c]
		// [vf]=[ 0  1 2tf   3tf^2  4tf^3  5tf^4][d]
		// [a0]=[ 0  0 2     6t0   12t0^2 20t0^3][e]
		// [af]=[ 0  0 2     6tf   12tf^2 20tf^3][f]
		matrix[0][0]=1;	matrix[0][1]=t0;	matrix[0][2]=  t02;	matrix[0][3]=  t03;	matrix[0][4]=   t04;	matrix[0][5]=   t05;
		matrix[1][0]=1;	matrix[1][1]=tf;	matrix[1][2]=  tf2;	matrix[1][3]=  tf3;	matrix[1][4]=   tf4;	matrix[1][5]=   tf5;
		matrix[2][0]=0;	matrix[2][1]= 1;	matrix[2][2]=2*t0;	matrix[2][3]=3*t02;	matrix[2][4]= 4*t03;	matrix[2][5]= 5*t04;
		matrix[3][0]=0;	matrix[3][1]= 1;	matrix[3][2]=2*tf;	matrix[3][3]=3*tf2;	matrix[3][4]= 4*tf3;	matrix[3][5]= 5*tf4;
		matrix[4][0]=0;	matrix[4][1]= 0;	matrix[4][2]=2;		matrix[4][3]=6*t0;	matrix[4][4]=12*t02;	matrix[4][5]=20*t03;
		matrix[5][0]=0;	matrix[5][1]= 0;	matrix[5][2]=2;		matrix[5][3]=6*tf;	matrix[5][4]=12*tf2;	matrix[5][5]=20*tf3;
		
		return matrix;
	}
	
	
	static final double ANGLE_STEP_SIZE=10.0000;
	
	/**
	 * Test SHIKSolver_RTTRTR and DHRobot_Sixi2.
	 * 
	 * In theory Inverse Kinematics (IK) can be given a matrix that, if solved for one solution, produces a set of values
	 * that can be fed to Forward Kinematics (FK) which should reproduce the original matrix.
	 * This test confirms that this theory is true for a wide range of valid angle values in the robot arm.
	 * Put another way, we use one set of matrix0=FK(angles), keyframe = IK(m0), m1=FK(keyframe), then confirm m1==m0.
	 * Remember keyframe might not equal angles because IK can produce more than one correct answer for the same matrix.
	 * 
	 * The code does not check for collisions.  
	 * The granularity of the testing is controlled by ANGLE_STEP_SIZE, which has a O^6 effect, so lower it very carefully.
	 */
	@Test
	public void testFK2IK() {
		System.out.println("testFK2IK()");
		DHRobot_Sixi2 robot = new DHRobot_Sixi2();
		int numLinks = robot.getNumLinks();
		assert(numLinks>0);

		DHIKSolver solver = robot.getSolverIK();
		DHKeyframe keyframe0 = (DHKeyframe)robot.createKeyframe();
		DHKeyframe keyframe1 = (DHKeyframe)robot.createKeyframe();
		Matrix4d m0 = new Matrix4d();
		Matrix4d m1 = new Matrix4d();
		
		// Find the min/max range for each joint
		DHLink link0 = robot.getLink(0);		double bottom0 = link0.rangeMin;		double top0 = link0.rangeMax;
		DHLink link1 = robot.getLink(1);		double bottom1 = link1.rangeMin;		double top1 = link1.rangeMax;
		DHLink link2 = robot.getLink(2);		double bottom2 = link2.rangeMin;		double top2 = link2.rangeMax;
		// link3 does not bend
		DHLink link4 = robot.getLink(4);		double bottom4 = link4.rangeMin;		double top4 = link4.rangeMax;
		DHLink link5 = robot.getLink(5);		double bottom5 = link5.rangeMin;		double top5 = link5.rangeMax;
		DHLink link6 = robot.getLink(6);		double bottom6 = link6.rangeMin;		double top6 = link6.rangeMax;

		int totalTests = 0;
		int totalOneSolutions = 0;
		int totalPasses = 0;
		
		double x,y,z;
		double u=(bottom4+top4)/2;
		double v=(bottom5+top5)/2;
		double w=(bottom6+top6)/2;

		BufferedWriter out=null;
		try {
			out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/test.txt")));
			out.write("x\ty\tz\tu\tv\tw\tJ0\tJ1\tJ2\tJ3\tJ4\tJ5\tResult\n");

			// go through the entire range of motion of the sixi 2 robot arm
			for(x=bottom0;x<top0;x+=ANGLE_STEP_SIZE) {
				keyframe0.fkValues[0]=x;
				for(y=bottom1;y<top1;y+=ANGLE_STEP_SIZE) {
					keyframe0.fkValues[1]=y;
					for(z=bottom2;z<top2;z+=ANGLE_STEP_SIZE) {
						keyframe0.fkValues[2]=z;
						for(u=bottom4;u<top4;u+=ANGLE_STEP_SIZE) 
						{
							keyframe0.fkValues[3]=u;
							for(v=bottom5;v<top5;v+=ANGLE_STEP_SIZE) 
							{
								keyframe0.fkValues[4]=v;
								//for(w=bottom6;w<top6;w+=ANGLE_STEP_SIZE) 
								{
									keyframe0.fkValues[5]=w;
									
									++totalTests;
									// use forward kinematics to find the endMatrix of the pose
				            		robot.setRobotPose(keyframe0);
									m0.set(robot.getEndMatrix());
									// now generate a set of FK values from the endMatrix m0.
									solver.solve(robot, m0, keyframe1);
									if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
										++totalOneSolutions;
										
										// update the robot pose and get the m1 matrix. 
					            		robot.setRobotPose(keyframe1);
					            		m1.set(robot.getEndMatrix());
					            		
					            		String message = StringHelper.formatDouble(keyframe0.fkValues[0])+"\t"
					            						+StringHelper.formatDouble(keyframe0.fkValues[1])+"\t"
					            						+StringHelper.formatDouble(keyframe0.fkValues[2])+"\t"
				            							+StringHelper.formatDouble(keyframe0.fkValues[3])+"\t"
				            							+StringHelper.formatDouble(keyframe0.fkValues[4])+"\t"
				            							+StringHelper.formatDouble(keyframe0.fkValues[5])+"\t"
				            							+StringHelper.formatDouble(keyframe1.fkValues[0])+"\t"
					            						+StringHelper.formatDouble(keyframe1.fkValues[1])+"\t"
					            						+StringHelper.formatDouble(keyframe1.fkValues[2])+"\t"
				            							+StringHelper.formatDouble(keyframe1.fkValues[3])+"\t"
				            							+StringHelper.formatDouble(keyframe1.fkValues[4])+"\t"
				            							+StringHelper.formatDouble(keyframe1.fkValues[5])+"\t";
					            		
					            		String error="";
					            		out.write(message);
					            		boolean bad=false;
					            		// it's possible that different fk values are generated but the final matrix is the same.

					            		// compare the m0 and m1 matrixes, which should be identical.
					            		if(!m1.epsilonEquals(m0, 1e-2)) {
					            			Matrix4d diff = new Matrix4d();
					            			diff.sub(m1,m0);
					            			Vector3d a0 = new Vector3d();
					            			Vector3d a1 = new Vector3d();
					            			m0.get(a0);
					            			m1.get(a1);
					            			a0.sub(a1);
					            			error+="Matrix "+a0.length();
					            			bad=true;
					            		}
					            		out.write(error+"\n");
					            		if(bad==false) {
					            			++totalPasses;
					            		}
									}
								}
							}
						}
						out.flush();
					}
				}
			}
			System.out.println("testFK2IK() total="+totalTests+", one solution="+totalOneSolutions+", passes="+totalPasses);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(out!=null) out.flush();
				if(out!=null) out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
