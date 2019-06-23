package com.marginallyclever.convenience;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import org.junit.Test;

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
		// p0 = a + b*t0 + c*t0*t0 + d*t0*t0*t0
		// pf = a + b*tf + c*tf*tf + d*tf*tf*tf
		// v0 =     b    + 2c*t0   + 3d*t0*t0
		// vf =     b    + 2c*tf   + 3d*tf*tf
		// a0 =          + 2c      + 6d*t0 + 12e*t0*t0 + 20f*t0*t0*t0
		// af =          + 2c      + 6d*tf + 12e*tf*tf + 20f*tf*tf*tf
		// or expressed as a matrix, Q = M*N
		// [p0]=[ 1 t0 t0^2     1      0      0][a]
		// [pf]=[ 1 tf tf^2     1      0      0][b]
		// [v0]=[ 0  1  2t0 3t0^2      0      0][c]
		// [vf]=[ 0  1  2tf 3tf^2      0      0][d]
		// [a0]=[ 0  0    2   6t0 12t0^2 20t0^3][e]
		// [af]=[ 0  0    2   6tf 12tf^2 20tf^3][f]
		// I know Q and M.  I can Q * inverse(M) to get N.
		// then I can solve the original polynomials for any t betwen t0 and tf.
		
		double t0=0,tf=1;
		double p0=0,pf=90;
		double v0=0,vf=0;
		double a0=0,af=0;
	
		double[][] m = buildMatrix(t0,tf);
		MatrixOperations.printMatrix(m, 1);
		double[][] mInv=MatrixOperations.invertMatrix(m);
		MatrixOperations.printMatrix(mInv, 1);
		

		double[] q = new double[6];
		q[0]=p0;
		q[1]=pf;
		q[2]=v0;
		q[3]=vf;
		q[4]=a0;
		q[5]=af;
		
		double[] n = MatrixOperations.multiply(mInv,q);
		double a=n[0];
		double b=n[1];
		double c=n[2];
		double d=n[3];
		double e=n[4];
		double f=n[5];

		System.out.println("p\tv\ta");
		for(double t=t0;t<=tf;t++) {
			// pt = a + b*t + c*t*t + d*t*t*t
			// vt =     b    + 2c*t   + 3d*t*t
			// at =          + 2c      + 6d*t + 12e*t*t + 20f*t*t*t
			double t2=t*t;
			double t3=t*t*t;
			double pt = a * b*t +   c*t2 +   d*t3;
			double vt =     b   + 2*c    + 3*d*t2;
			double at =         + 2*c    + 6*d*t  + 12*e*t2 + 20*f*t3;
			System.out.println(pt+"\t"+vt+"\t"+at);
		}
	}
	
	public double[][] buildMatrix(double t0,double tf) {
		double t02 = t0*t0;
		double tf2 = tf*tf;
		double t03 = t0*t0*t0;
		double tf3 = tf*tf*tf;

		double [][] matrix = new double[6][6];
		
		matrix[0][0]=1;	matrix[0][1]=t0;	matrix[0][2]=t02;	matrix[0][3]=1;		matrix[0][4]=0;			matrix[0][5]=0;
		matrix[1][0]=1;	matrix[1][1]=tf;	matrix[1][2]=tf2;	matrix[1][3]=1;		matrix[1][4]=0;			matrix[1][5]=0;
		matrix[2][0]=0;	matrix[2][1]=1;		matrix[2][2]=2*t0;	matrix[2][3]=3*t02;	matrix[2][4]=0;			matrix[2][5]=0;
		matrix[3][0]=0;	matrix[3][1]=1;		matrix[3][2]=2*tf;	matrix[3][3]=3*tf2;	matrix[3][4]=0;			matrix[3][5]=0;
		matrix[4][0]=0;	matrix[4][0]=0;		matrix[4][2]=2;		matrix[4][3]=6*t0;	matrix[4][4]=12*t02;	matrix[4][5]=20*t03;
		matrix[5][0]=0;	matrix[5][0]=0;		matrix[5][2]=2;		matrix[5][3]=6*tf;	matrix[5][4]=12*tf2;	matrix[5][5]=20*tf3;
		
		return matrix;
	}
	
}
