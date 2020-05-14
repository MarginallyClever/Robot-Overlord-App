package com.marginallyclever.robotOverlord;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.junit.Test;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2Sim;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTTRTR;

public class MiscTests {
	@Test
	public void testChecksums() {
		//>>G0 X0.000 Y-86.789 Z27.498 U0.000 V-30.692 W0.000*78
		//<<G0 X0.000 Y-86.789 Z27.4U0.000 V-30.692 W0.000*78
		//<<BADCHECKSUM calc=111 sent=78
		
		String a = StringHelper.generateChecksum("G0 X0.000 Y-86.789 Z27.498 U0.000 V-30.692 W0.000");
		String b = StringHelper.generateChecksum("G0 X0.000 Y-86.789 Z27.4U0.000 V-30.692 W0.000");
		System.out.println("a="+a);
		System.out.println("b="+b);
		assert(a.equals("*78"));
		System.out.println("test a passed");
		assert(b.equals("*111"));
		System.out.println("test b passed");
	}
	
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
	 * See https://www.eecs.yorku.ca/course_archive/2017-18/W/4421/lectures/Inverse%20kinematics%20-%20annotated.pdf
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
		double[][] mInv=MatrixHelper.invertMatrix(m);
		double[] n = MatrixHelper.multiply(mInv,q);

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
	
	
	static final double ANGLE_STEP_SIZE=30.0000;
	
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
		Sixi2 robot = new Sixi2();
		int numLinks = robot.sim.getNumLinks();
		assert(numLinks>0);

		DHIKSolver solver = new DHIKSolver_RTTRTR();
		DHKeyframe keyframe0 = robot.sim.getIKSolver().createDHKeyframe();
		DHKeyframe keyframe1 = robot.sim.getIKSolver().createDHKeyframe();
		Matrix4d m0 = new Matrix4d();
		Matrix4d m1 = new Matrix4d();
		
		// Find the min/max range for each joint
		DHLink link0 = robot.sim.getLink(0);		double bottom0 = link0.getRangeMin();		double top0 = link0.getRangeMax();
		DHLink link1 = robot.sim.getLink(1);		double bottom1 = link1.getRangeMin();		double top1 = link1.getRangeMax();
		DHLink link2 = robot.sim.getLink(2);		double bottom2 = link2.getRangeMin();		double top2 = link2.getRangeMax();
		// link3 does not bend
		DHLink link4 = robot.sim.getLink(4);		double bottom4 = link4.getRangeMin();		double top4 = link4.getRangeMax();
		DHLink link5 = robot.sim.getLink(5);		double bottom5 = link5.getRangeMin();		double top5 = link5.getRangeMax();
		DHLink link6 = robot.sim.getLink(6);		double bottom6 = link6.getRangeMin();		double top6 = link6.getRangeMax();

		int totalTests = 0;
		int totalOneSolutions = 0;
		int totalPasses = 0;
		
		double x,y,z;
		double u=(bottom4+top4)/2;
		double v=(bottom5+top5)/2;
		double w=(bottom6+top6)/2;

		BufferedWriter out=null;
		try {
			out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/test.csv")));
			out.write("x\ty\tz\tu\tv\tw\tJ0\tJ1\tJ2\tJ3\tJ4\tJ5\tResult\n");

			// go through the entire range of motion of the sixi 2 robot arm
			for(x=bottom0;x<top0;x+=ANGLE_STEP_SIZE) {
				keyframe0.fkValues[0]=x;
				for(y=bottom1;y<top1;y+=ANGLE_STEP_SIZE) {
					keyframe0.fkValues[1]=y;
					for(z=bottom2;z<top2;z+=ANGLE_STEP_SIZE) {
						keyframe0.fkValues[2]=z;
						for(u=bottom4;u<top4;u+=ANGLE_STEP_SIZE) {
							keyframe0.fkValues[3]=u;
							for(v=bottom5;v<top5;v+=ANGLE_STEP_SIZE) {
								keyframe0.fkValues[4]=v;
								for(w=bottom6;w<top6;w+=ANGLE_STEP_SIZE) {
									keyframe0.fkValues[5]=w;
									
									++totalTests;
									// use forward kinematics to find the endMatrix of the pose
				            		robot.sim.setPoseFK(keyframe0);
									m0.set(robot.sim.endEffector.getPoseWorld());
									// now generate a set of FK values from the endMatrix m0.
									if(solver.solve(robot.sim, m0, keyframe1)==DHIKSolver.SolutionType.ONE_SOLUTION) {
										++totalOneSolutions;
										
										// update the robot pose and get the m1 matrix. 
					            		robot.sim.setPoseFK(keyframe1);
					            		m1.set(robot.sim.endEffector.getPoseWorld());
					            		
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
					            			Quat4d dq=new Quat4d();
					            			diff.sub(m1,m0);
					            			diff.get(dq);
					            			Vector3d p0 = new Vector3d();
					            			Vector3d p1 = new Vector3d();
					            			Vector3d dp = new Vector3d();
					            			m0.get(p0);
					            			m1.get(p1);
					            			dp.sub(p1,p0);
					            			error+="Matrix "+dp.length()+"@"+dq.toString();
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

	/**
	 * Plot points along the workspace boundary for the sixi robot in the XZ plane.
	 */
	//@Test
	public void plotXZ() {
		System.out.println("plotXZ()");
		Sixi2 robot = new Sixi2();
		int numLinks = robot.sim.getNumLinks();
		assert(numLinks>0);

		// Find the min/max range for each joint
		DHLink link0 = robot.sim.getLink(0);  double bottom0 = link0.getRangeMin();  double top0 = link0.getRangeMax();  double mid0 = (top0+bottom0)/2;
		DHLink link1 = robot.sim.getLink(1);  double bottom1 = link1.getRangeMin();  double top1 = link1.getRangeMax();  double mid1 = (top1+bottom1)/2;
		DHLink link2 = robot.sim.getLink(2);  double bottom2 = link2.getRangeMin();  double top2 = link2.getRangeMax();//double mid2 = (top2+bottom2)/2;
		// link3 does not bend
		DHLink link4 = robot.sim.getLink(4);  double bottom4 = link4.getRangeMin();//double top4 = link4.getRangeMax();  double mid4 = (top4+bottom4)/2;  
		DHLink link5 = robot.sim.getLink(5);  double bottom5 = link5.getRangeMin();  double top5 = link5.getRangeMax();  double mid5 = (top5+bottom5)/2;  
		DHLink link6 = robot.sim.getLink(6);  double bottom6 = link6.getRangeMin();//double top6 = link6.getRangeMax();  double mid6 = (top6+bottom6)/2;  

		BufferedWriter out=null;
		try {
			out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/plotxz.csv")));
			out.write("X\tY\tZ\n");

			// go through the entire range of motion of the sixi 2 robot arm
			double ANGLE_STEP_SIZE2=1;
			
			double x=mid0;
			double y=bottom1;
			double z=bottom2;
			double u=bottom4;
			double v=bottom5;
			double w=bottom6;

			for(v=bottom5;v<mid5;v+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // picasso box to middle
			for(y=bottom1;y<mid1;y+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot); // shoulder forward
			// skip j0 to keep things on the XZ plane.
			for(;y<top1;y+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // shoulder forward 
			for(;z<top2;z+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // elbow forward  
			for(;v<top5;v+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // picasso box forward

			for(;y>bottom1;y-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // shoulder back 
			for(;z>bottom2;z-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // elbow back  
			for(;v<bottom5;v-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);  // picasso box back
			
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

	/**
	 * Plot points along the workspace boundary for the sixi robot in the XY plane.
	 */
	//@Test
	public void plotXY() {
		System.out.println("plotXY()");
		Sixi2 robot = new Sixi2();
		int numLinks = robot.sim.getNumLinks();
		assert(numLinks>0);

		// Find the min/max range for each joint
		DHLink link0 = robot.sim.getLink(0);  double bottom0 = link0.getRangeMin();  double top0 = link0.getRangeMax();//double mid0 = (top0+bottom0)/2;
		DHLink link1 = robot.sim.getLink(1);/*double bottom1 = link1.getRangeMin();*/double top1 = link1.getRangeMax();//double mid1 = (top1+bottom1)/2;
		DHLink link2 = robot.sim.getLink(2);  double bottom2 = link2.getRangeMin();  double top2 = link2.getRangeMax();//double mid2 = (top2+bottom2)/2;
		// link3 does not bend
		DHLink link4 = robot.sim.getLink(4);  double bottom4 = link4.getRangeMin();  double top4 = link4.getRangeMax();  double mid4 = (top4+bottom4)/2;  
		DHLink link5 = robot.sim.getLink(5);  double bottom5 = link5.getRangeMin();  double top5 = link5.getRangeMax();  double mid5 = (top5+bottom5)/2;  
		DHLink link6 = robot.sim.getLink(6);  double bottom6 = link6.getRangeMin();  double top6 = link6.getRangeMax();  double mid6 = (top6+bottom6)/2;  

		double ANGLE_STEP_SIZE2=1;
		
		BufferedWriter out=null;
		try {
			out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/plotxy.csv")));
			out.write("X\tY\tZ\n");

			// go through the entire range of motion of the sixi 2 robot arm
			// stretch arm forward as much as possible.
			double x=bottom0;
			double y=top1;
			double z=bottom2;
			double u=mid4;
			double v=mid5;
			double w=mid6;

			for(x=bottom0;x<top0;x+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);
			for(;z<top2;z+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);
			//for(;v<top5;v+=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);
			
			for(x=top0;x>bottom0;x-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);
			//for(;v>mid5;v-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);
			for(;z>bottom2;z-=ANGLE_STEP_SIZE2) plot(x,y,z,u,v,w,out,robot);
			
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
	
	/**
	 * Used by plotXY() and plotXZ()
	 * @param x
	 * @param y
	 * @param z
	 * @param u
	 * @param v
	 * @param w
	 * @param out
	 * @param robot
	 * @throws IOException
	 */
	private void plot(double x,double y,double z,double u,double v,double w,BufferedWriter out,Sixi2 robot) throws IOException {
		DHKeyframe keyframe0 = robot.sim.getIKSolver().createDHKeyframe();
		Matrix4d m0 = new Matrix4d();
		
		keyframe0.fkValues[0]=x;
		keyframe0.fkValues[1]=y;
		keyframe0.fkValues[2]=z;
		keyframe0.fkValues[3]=u;
		keyframe0.fkValues[4]=v;
		keyframe0.fkValues[5]=w;
					
		// use forward kinematics to find the endMatrix of the pose
		robot.sim.setPoseFK(keyframe0);
		m0.set(robot.sim.endEffector.getPoseWorld());
		
		String message = StringHelper.formatDouble(m0.m03)+"\t"
						+StringHelper.formatDouble(m0.m13)+"\t"
						+StringHelper.formatDouble(m0.m23)+"\n";
   		out.write(message);
	}
	
	/**
	 * Report Jacobian results for a given pose
	 */
	@Test
	public void approximateJacobianMatrix() {
		System.out.println("approximateJacobianMatrix()");
		Sixi2 robot = new Sixi2();

		// Find the min/max range for each joint
		DHLink link0 = robot.sim.getLink(0);  double bottom0 = link0.getRangeMin();  double top0 = link0.getRangeMax();  double mid0 = (top0+bottom0)/2;
		DHLink link1 = robot.sim.getLink(1);  double bottom1 = link1.getRangeMin();  double top1 = link1.getRangeMax();  double mid1 = (top1+bottom1)/2;
		DHLink link2 = robot.sim.getLink(2);  double bottom2 = link2.getRangeMin();  double top2 = link2.getRangeMax();  double mid2 = (top2+bottom2)/2;
		// link3 does not bend
		DHLink link4 = robot.sim.getLink(4);  double bottom4 = link4.getRangeMin();  double top4 = link4.getRangeMax();  double mid4 = (top4+bottom4)/2;  
		DHLink link5 = robot.sim.getLink(5);  double bottom5 = link5.getRangeMin();  double top5 = link5.getRangeMax();  double mid5 = (top5+bottom5)/2;  
		DHLink link6 = robot.sim.getLink(6);  double bottom6 = link6.getRangeMin();  double top6 = link6.getRangeMax();  double mid6 = (top6+bottom6)/2;  

		BufferedWriter out=null;
		try {
			out = new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/jacobian.csv")));

			DHKeyframe keyframe = robot.sim.getIKSolver().createDHKeyframe();
			// set the pose with fk
			keyframe.fkValues[0]=mid0;
			keyframe.fkValues[1]=mid1;
			keyframe.fkValues[2]=mid2;
			keyframe.fkValues[3]=mid4;
			keyframe.fkValues[4]=mid5;
			keyframe.fkValues[5]=mid6;

			assert( robot.sim instanceof Sixi2Sim );
			Sixi2Sim sim = (Sixi2Sim)(robot.sim);
			double [][] jacobian = sim.approximateJacobian(keyframe);
			
			int i,j;
			for(i=0;i<6;++i) {
				for(j=0;j<6;++j) {
					out.write(jacobian[i][j]+"\t");
				}
				out.write("\n");
			}
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
	/**
	 * Use Jacobian matrixes to find joint velocity over time.
	 */
	@Test
	public void jointVelocityOverTime() {
		System.out.println("angularVelocityOverTime()");
		Sixi2 robot = new Sixi2();
		// a new sixi starts with the ghost post in the home position
		// and the live pose in the rest position.

		assert( robot.sim instanceof Sixi2Sim );
		Sixi2Sim sim = (Sixi2Sim)(robot.sim);
	
		try(BufferedWriter out=new BufferedWriter(new FileWriter(new File("c:/Users/Admin/Desktop/jvot.csv")))) {
			out.write("Px\tPy\tPz\tJ0\tJ1\tJ2\tJ3\tJ4\tJ5\n");
			
			DHKeyframe keyframe = sim.getIKSolver().createDHKeyframe();	

			final double dt=0.03f;  // time step
			// match the force directions with the start and end matrix
			// or the calculation will never work.
			Vector3d dp = new Vector3d(0,3,0);	// linear force
			Vector3d r  = new Vector3d(0,0,0);	// rotation force
			final double [] force = {dp.x,dp.y,dp.z,r.x,r.y,r.z};
	
			// set a new position
			Matrix4d m = sim.endEffector.getPoseWorld();
			m.m13=-20;
			m.m23-=5;
			
			int j;
			int safety=0;
			int failed=0;
			
			// until we get to position or something has gone wrong
			while(m.m13<20 && safety<10000) {
				safety++;
				System.out.print(safety+": ");
				if(sim.setPoseIK(m)) {
					sim.getPoseFK(keyframe);
					// matrix m has a sane solution (is reachable)
					double [][] jacobian = sim.approximateJacobian(keyframe);
					double [][] inverseJacobian = MatrixHelper.invert(jacobian);
					
					out.write(m.m03+"\t"+m.m13+"\t"+m.m23+"\t");
					double [] jvot = new double[6];
					for(j=0;j<6;++j) {
						for(int k=0;k<6;++k) {
							jvot[j]+=inverseJacobian[k][j]*force[k];
						}

						out.write(Math.toDegrees(jvot[j])+"\t");	
						if (!Double.isNaN(jvot[j])) {
							double v = keyframe.fkValues[j] + Math.toDegrees(jvot[j]) * dt;
							keyframe.fkValues[j]=MathHelper.capRotationDegrees(v,0);
						}
					}
					out.write("\n");

					// if the new pose is sane,
					if (robot.sim.sanityCheck(keyframe)) {
						// set the pose (eg, move each joints by jvot*dt)
						robot.sim.setPoseFK(keyframe);
						m = robot.sim.endEffector.getPoseWorld();

						System.out.println("ok");
						continue;
					}
				}
				// the pose was not sane, move the matrix and hope we slip past the singularity.
				System.out.println("bad");
				failed++;
				m.m03+=force[0]*dt;
				m.m13+=force[1]*dt;
				m.m23+=force[2]*dt;
			}

			System.out.println(failed+" bad of "+safety+" tests.");
			out.flush();
			out.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
