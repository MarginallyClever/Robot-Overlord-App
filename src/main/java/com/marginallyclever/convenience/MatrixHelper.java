package com.marginallyclever.convenience;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.junit.Test;

import com.jogamp.opengl.GL2;

public class MatrixHelper {	
	/**
	 * See drawMatrix(gl2,p,u,v,w,1)
	 * @param gl2
	 * @param m
	 * @param scale
	 */
	public static void drawMatrix(GL2 gl2,Matrix4d m,double scale) {
		boolean depthWasOn = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		boolean lightWasOn = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		gl2.glPushMatrix();
			gl2.glTranslated(m.m03,m.m13,m.m23);
			gl2.glScaled(scale, scale, scale);
			
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,1,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m00,m.m10,m.m20);  // 1,1,0 = yellow
			gl2.glColor3f(0,1,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m01,m.m11,m.m21);  // 0,1,1 = teal 
			gl2.glColor3f(1,0,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m02,m.m12,m.m22);  // 1,0,1 = magenta
			gl2.glEnd();
	
		gl2.glPopMatrix();
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
	}
	
	/**
	 * See drawMatrix(gl2,p,u,v,w,1)
	 * @param gl2
	 * @param m
	 * @param scale
	 */
	public static void drawMatrix2(GL2 gl2,Matrix4d m,double scale) {
		boolean depthWasOn = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		boolean lightWasOn = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		gl2.glPushMatrix();
			gl2.glTranslated(m.m03,m.m13,m.m23);
			gl2.glScaled(scale, scale, scale);
			
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m00,m.m10,m.m20);  // 1,1,0 = yellow
			gl2.glColor3f(0,1,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m01,m.m11,m.m21);  // 0,1,1 = teal 
			gl2.glColor3f(0,0,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m02,m.m12,m.m22);  // 1,0,1 = magenta
			gl2.glEnd();
	
		gl2.glPopMatrix();
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
	}

	
	/**
	 * See drawMatrix(gl2,p,u,v,w,1)
	 */
	public static void drawMatrix(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w) {
		drawMatrix(gl2,p,u,v,w,1);
	}
	
	/**
	 * Draw the three vectors of a matrix at a point
	 * @param gl2 render context
	 * @param p position at which to draw
	 * @param u in yellow (1,1,0)
	 * @param v in teal (0,1,1)
	 * @param w in magenta (1,0,1)
	 * @param scale nominally 1
	 */
	public static void drawMatrix(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w,double scale) {
		Matrix4d m = new Matrix4d(
				u.x,u.y,u.z,p.x,
				v.x,v.y,v.z,p.y,
				w.x,w.y,w.z,p.z,
				0,0,0,1.0
				);
		drawMatrix(gl2,m,scale);
	}

	/**
	 * Same as drawMatrix, but with alternate colors
	 * See drawMatrix(gl2,p,u,v,w,1)
	 * @param gl2
	 * @param p
	 * @param u
	 * @param v
	 * @param w
	 */
	public static void drawMatrix2(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w) {
		drawMatrix2(gl2,p,u,v,w,1);
	}
	
	/**
	 * Same as drawMatrix, but with alternate colors
	 * Draw the three vectors of a matrix at a point
	 * @param gl2 render context
	 * @param p position at which to draw
	 * @param u in red
	 * @param v in green
	 * @param w in blue
	 * @param scale nominally 1
	 */
	public static void drawMatrix2(GL2 gl2,Vector3d p,Vector3d u,Vector3d v,Vector3d w,double scale) {
		Matrix4d m = new Matrix4d(
				u.x,u.y,u.z,p.x,
				v.x,v.y,v.z,p.y,
				w.x,w.y,w.z,p.z,
				0,0,0,1.0
				);
		drawMatrix2(gl2,m,scale);
	}
	
	/**
	 * Confirms that this matrix is a rotation matrix.  Matrix A * transpose(A) should be the Identity.
	 * See also https://www.learnopencv.com/rotation-matrix-to-euler-angles/
	 * @param mat
	 * @return
	 */
	public static boolean isRotationMatrix(Matrix3d mat) {
		Matrix3d m1 = new Matrix3d(mat);
		Matrix3d m2 = new Matrix3d();
		m2.transpose(m1);
		m1.mul(m2);
		m2.setIdentity();
		return m1.epsilonEquals(m2, 1e-6);
	}
	
	/**
	 * Convert a matrix to Euler rotations.  There are many valid solutions.
	 * See also https://www.learnopencv.com/rotation-matrix-to-euler-angles/
	 * @param mat the Matrix3d to convert.
	 * @return a Vector3d resulting radian rotations.  One possible solution.
	 */
	public static Vector3d matrixToEuler(Matrix3d mat) {
		assert(isRotationMatrix(mat));
		
		double sy = Math.sqrt(mat.m00*mat.m00 + mat.m10*mat.m10);
		boolean singular = sy < 1e-6;
		double x,y,z;
		if(!singular) {
			x = Math.atan2( mat.m21,mat.m22);
			y = Math.atan2(-mat.m20,sy);
			z = Math.atan2( mat.m10,mat.m00);
		} else {                   
			x = Math.atan2(-mat.m12, mat.m11);
			y = Math.atan2(-mat.m20, sy);
			z = 0;
		}
		return new Vector3d(x,y,z);
	}
	
	/**
	 * Convert euler rotations to a matrix.  See also https://www.learnopencv.com/rotation-matrix-to-euler-angles/
	 * @param v radian rotation values
	 * @return Matrix3d resulting matrix
	 */
	public static Matrix3d eulerToMatrix(Vector3d v) {
		double c0 = Math.cos(v.x);		double s0 = Math.sin(v.x);
		double c1 = Math.cos(v.y);		double s1 = Math.sin(v.y);
		double c2 = Math.cos(v.z);		double s2 = Math.sin(v.z);
		
		Matrix3d rX=new Matrix3d( 1,  0, 0,
								  0,c0,-s0,
								  0,s0, c0);
		Matrix3d rY=new Matrix3d(c1,  0,s1,
								  0,  1, 0,
								-s1,  0,c1);
		Matrix3d rZ=new Matrix3d(c2,-s2, 0,
				                 s2, c2, 0,
				                  0,  0, 1);

		Matrix3d result = new Matrix3d();
		Matrix3d interim = new Matrix3d();
		interim.mul(rY,rX);
		result.mul(rZ,interim);

		return result;
	}
	
	@Test
	public void testEulerMatrix() {
		Vector3d v2;
		Vector3d v1 = new Vector3d();
		for(int i=0;i<1000;++i) {
			v1.x = Math.random() * Math.PI*2.0;
			v1.y = Math.random() * Math.PI*2.0;
			v1.z = Math.random() * Math.PI*2.0;
			
			Matrix3d a = eulerToMatrix(v1);
			v2 = matrixToEuler(a);
			Matrix3d b = eulerToMatrix(v2);
			
			boolean test = b.epsilonEquals(a, 1e-6);
			assert(test);
			if(test==false) {
				System.out.println(i+"a="+a);
				System.out.println(i+"b="+b);
				b.sub(a);
				System.out.println(i+"d="+b);
			}
			org.junit.Assert.assertTrue(test);
		}
		System.out.println("testEulerMatrix() OK");
	}
	
	/**
	 * Interpolate between two 4d matrixes, (end-start)*i + start where i=[0...1]
	 * @param start start matrix
	 * @param end end matrix
	 * @param alpha double value in the range [0...1]
	 * @param result where to store the resulting matrix
	 * @return True if the operation succeeds.  False if the inputs are bad or the operation fails. 
	 */
	public static boolean interpolate(Matrix4d start,Matrix4d end,double alpha,Matrix4d result) {
		if(alpha<0 || alpha>1) return false;
		// spherical interpolation (slerp) between the two matrix orientations
		Quat4d qStart = new Quat4d();
		start.get(qStart);
		Quat4d qEnd = new Quat4d();
		end.get(qEnd);
		qStart.interpolate(qEnd, alpha);
		// linear interpolation between the two matrix translations
		Vector3d tStart = new Vector3d();
		start.get(tStart);
		Vector3d tEnd = new Vector3d();
		end.get(tEnd);
		tStart.interpolate(tEnd, alpha);
		// build the result matrix
		result.set(qStart);
		result.setTranslation(tStart);
		// report ok
		return true;
	}
	
	public static void applyMatrix(GL2 gl2,Matrix4d pose) {
		double[] mat = new double[16];
		mat[ 0] = pose.m00;
		mat[ 1] = pose.m10;
		mat[ 2] = pose.m20;
		mat[ 3] = pose.m30;
		mat[ 4] = pose.m01;
		mat[ 5] = pose.m11;
		mat[ 6] = pose.m21;
		mat[ 7] = pose.m31;
		mat[ 8] = pose.m02;
		mat[ 9] = pose.m12;
		mat[10] = pose.m22;
		mat[11] = pose.m32;
		mat[12] = pose.m03;
		mat[13] = pose.m13;
		mat[14] = pose.m23;
		mat[15] = pose.m33;
		
		gl2.glMultMatrixd(mat, 0);	
	} 

	
	/**
	 * invert an N*N matrix.
	 * From https://www.sanfoundry.com/java-program-find-inverse-matrix/
	 * 
	 * @param a
	 * @return the inverse.
	 */
	public static double[][] invert(double a[][]) {
		int n = a.length;
		double x[][] = new double[n][n];
		double b[][] = new double[n][n];
		int index[] = new int[n];
		for (int i = 0; i < n; ++i)
			b[i][i] = 1;

		// Transform the matrix into an upper triangle
		gaussian(a, index);

		// Update the matrix b[i][j] with the ratios stored
		for (int i = 0; i < n - 1; ++i)
			for (int j = i + 1; j < n; ++j)
				for (int k = 0; k < n; ++k)
					b[index[j]][k] -= a[index[j]][i] * b[index[i]][k];

		// Perform backward substitutions
		for (int i = 0; i < n; ++i) {
			x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];
			for (int j = n - 2; j >= 0; --j) {
				x[j][i] = b[index[j]][i];
				for (int k = j + 1; k < n; ++k) {
					x[j][i] -= a[index[j]][k] * x[k][i];
				}
				x[j][i] /= a[index[j]][j];
			}
		}
		return x;
	}

	/**
	 * Method to carry out the partial-pivoting Gaussian elimination.
	 * From https://www.sanfoundry.com/java-program-find-inverse-matrix/
	 * 
	 * @param a the matrix
	 * @param index the pivoting order.
	 */
	public static void gaussian(double a[][], int index[]) {
		int n = index.length;
		double c[] = new double[n];

		// Initialize the index
		for (int i = 0; i < n; ++i)
			index[i] = i;

		// Find the rescaling factors, one from each row
		for (int i = 0; i < n; ++i) {
			double c1 = 0;
			for (int j = 0; j < n; ++j) {
				double c0 = Math.abs(a[i][j]);
				if (c0 > c1)
					c1 = c0;
			}
			c[i] = c1;
		}

		// Search the pivoting element from each column
		int k = 0;
		for (int j = 0; j < n - 1; ++j) {
			double pi1 = 0;
			for (int i = j; i < n; ++i) {
				double pi0 = Math.abs(a[index[i]][j]);
				pi0 /= c[index[i]];
				if (pi0 > pi1) {
					pi1 = pi0;
					k = i;
				}
			}

			// Interchange rows according to the pivoting order
			int itmp = index[j];
			index[j] = index[k];
			index[k] = itmp;
			for (int i = j + 1; i < n; ++i) {
				double pj = a[index[i]][j] / a[index[j]][j];

				// Record pivoting ratios below the diagonal
				a[index[i]][j] = pj;

				// Modify other elements accordingly
				for (int l = j + 1; l < n; ++l)
					a[index[i]][l] -= pj * a[index[j]][l];
			}
		}
	}
}
