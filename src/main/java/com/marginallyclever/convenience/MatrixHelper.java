package com.marginallyclever.convenience;

import java.text.MessageFormat;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.log.Log;

/**
 * Convenience methods for matrixes
 * @author aggra
 *
 */
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
			gl2.glColor3f(1,0,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m00,m.m10,m.m20);  // 1,0,0 = red
			gl2.glColor3f(0,1,0);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m01,m.m11,m.m21);  // 0,1,0 = green 
			gl2.glColor3f(0,0,1);		gl2.glVertex3f(0,0,0);		gl2.glVertex3d(m.m02,m.m12,m.m22);  // 0,0,1 = blue
			gl2.glEnd();
	
		gl2.glPopMatrix();
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
	}

	public static void drawMatrix(GL2 gl2,double scale) {
		Matrix4d m= new Matrix4d();
		m.setIdentity();
		drawMatrix(gl2,m,scale);
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
	 * Eulers are using the ZYX convention.
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
	 * Eulers are using the ZYX convention.
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
	 * Convenience method to call matrixToEuler() with only the rotational component.
	 * Assumes the rotational component is a valid rotation matrix.
	 * Eulers are using the ZYX convention.
	 * @param mat
	 * @return a valid Euler solution to the matrix.
	 */
	public static Vector3d matrixToEuler(Matrix4d mat) {
		Matrix3d m3 = new Matrix3d();
		mat.get(m3);
		return matrixToEuler(m3);
	}
	
	/**
	 * Convert Euler rotations to a matrix.
	 * See also https://www.learnopencv.com/rotation-matrix-to-euler-angles/
	 * Eulers are using the ZYX convention.
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
	
	/**
	 * Interpolate between two 4d matrixes, (end-start)*i + start where i=[0...1]
	 * @param start start matrix
	 * @param end end matrix
	 * @param alpha double value in the range [0...1]
	 * @param result where to store the resulting matrix
	 * @return True if the operation succeeds.  False if the inputs are bad or the operation fails. 
	 */
	public static boolean interpolate(final Matrix4d start,final Matrix4d end,double alpha,Matrix4d result) {
		if(alpha<0 || alpha>1) return false;
		
		// spherical interpolation (slerp) between the two matrix orientations
		Quat4d qStart = new Quat4d();
		qStart.set(start);
		Quat4d qEnd = new Quat4d();
		qEnd.set(end);
		Quat4d qInter = new Quat4d();
		qInter.interpolate(qStart, qEnd, alpha);
		
		// linear interpolation between the two matrix translations
		Vector3d tStart = new Vector3d();
		start.get(tStart);
		Vector3d tEnd = new Vector3d();
		end.get(tEnd);
		Vector3d tInter = new Vector3d();
		tInter.interpolate(tStart, tEnd, alpha);
		
		// build the result matrix
		result.set(qInter);
		result.setTranslation(tInter);
		
		// report ok
		return true;
	}
	
	// cumulative multiplication of matrixes
	public static void applyMatrix(GL2 gl2,Matrix4d pose) {
		double[] mat = {
			pose.m00,pose.m10,pose.m20,pose.m30,
			pose.m01,pose.m11,pose.m21,pose.m31,
			pose.m02,pose.m12,pose.m22,pose.m32,
			pose.m03,pose.m13,pose.m23,pose.m33
		};
		
		gl2.glMultMatrixd(mat, 0);	
	} 
	
	public static void setMatrix(GL2 gl2,Matrix4d pose) {
		double[] mat = {
				pose.m00,pose.m10,pose.m20,pose.m30,
				pose.m01,pose.m11,pose.m21,pose.m31,
				pose.m02,pose.m12,pose.m22,pose.m32,
				pose.m03,pose.m13,pose.m23,pose.m33
			};
		
		gl2.glLoadMatrixd(mat, 0);	
	}

	/**
	 * invert an N*N matrix.
	 * @see https://github.com/rchen8/algorithms/blob/master/Matrix.java
	 * 
	 * @param a the matrix to invert.
	 * @return the result.
	 */
	public static double[][] invert(double a[][]) {
		double[][] inverse = new double[a.length][a.length];

		// minors and cofactors
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				inverse[i][j] = Math.pow(-1, i + j)
						* determinant(minor(a, i, j));

		// adjugate and determinant
		double det = 1.0 / determinant(a);
		for (int i = 0; i < inverse.length; i++) {
			for (int j = 0; j <= i; j++) {
				double temp = inverse[i][j];
				inverse[i][j] = inverse[j][i] * det;
				inverse[j][i] = temp * det;
			}
		}

		return inverse;
	}
	
	public static double determinant(double[][] matrix) {
		if (matrix.length != matrix[0].length)
			throw new IllegalStateException("invalid dimensions");

		if (matrix.length == 2)
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

		double det = 0;
		for (int i = 0; i < matrix[0].length; i++)
			det += Math.pow(-1, i) * matrix[0][i]
					* determinant(minor(matrix, 0, i));
		return det;
	}
	
	private static double[][] minor(double[][] matrix, int row, int column) {
		double[][] minor = new double[matrix.length - 1][matrix.length - 1];

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; i != row && j < matrix[i].length; j++)
				if (j != column)
					minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
		return minor;
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

	/**
	 * Method that multiplies two matrices and returns the result
	 *
	 * @param x first matrix
	 * @param y second matrix
	 *
	 * @return result after multiplication
	 */
	public static int[][] multiplyMatrices (int[][] x, int[][] y) {
		int[][] result;
		int xColumns, xRows, yColumns, yRows;

		xRows = x.length;
		xColumns = x[0].length;
		yRows = y.length;
		yColumns = y[0].length;
		result = new int[xRows][yColumns];

		if (xColumns != yRows) {
			throw new IllegalArgumentException (
					MessageFormat.format ("Matrices don't match: {0} != {1}.", xColumns, yRows));
		}


		for (int i = 0; i < xRows; i++) {
			for (int j = 0; j < yColumns; j++) {
				for (int k = 0; k < xColumns; k++) {
					result[i][j] += (x[i][k] * y[k][j]);
				}
			}
		}

		return (result);
	}

	/**
	 * Method that calculates determinant of given matrix
	 *
	 * @param matrix matrix of which we need to know determinant
	 *
	 * @return determinant of given matrix
	 */
	public static double matrixDeterminant (double[][] matrix) {
		double temporary[][];
		double result = 0;

		if (matrix.length == 1) {
			result = matrix[0][0];
			return (result);
		}

		if (matrix.length == 2) {
			result = ((matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]));
			return (result);
		}

		for (int i = 0; i < matrix[0].length; i++) {
			temporary = new double[matrix.length - 1][matrix[0].length - 1];

			for (int j = 1; j < matrix.length; j++) {
				for (int k = 0; k < matrix[0].length; k++) {
					if (k < i) {
						temporary[j - 1][k] = matrix[j][k];
					} else if (k > i) {
						temporary[j - 1][k - 1] = matrix[j][k];
					}
				}
			}

			result += matrix[0][i] * Math.pow (-1, (double) i) * matrixDeterminant (temporary);
		}
		return (result);
	}

    // matrix-vector multiplication (y = A * x)
    public static double[] multiply(double[][] a, double[] x) {
        int m = a.length;
        int n = a[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += a[i][j] * x[j];
        return y;
    }
    
	public static double[][] invertMatrix (double[][] matrix) {
		double[][] auxiliaryMatrix, invertedMatrix;
		int[] index;

		auxiliaryMatrix = new double[matrix.length][matrix.length];
		invertedMatrix = new double[matrix.length][matrix.length];
		index = new int[matrix.length];

		for (int i = 0; i < matrix.length; ++i) {
			auxiliaryMatrix[i][i] = 1;
		}

		transformToUpperTriangle (matrix, index);

		for (int i = 0; i < (matrix.length - 1); ++i) {
			for (int j = (i + 1); j < matrix.length; ++j) {
				for (int k = 0; k < matrix.length; ++k) {
					auxiliaryMatrix[index[j]][k] -= matrix[index[j]][i] * auxiliaryMatrix[index[i]][k];
				}
			}
		}

		for (int i = 0; i < matrix.length; ++i) {
			invertedMatrix[matrix.length - 1][i] = (auxiliaryMatrix[index[matrix.length - 1]][i] /
					matrix[index[matrix.length - 1]][matrix.length - 1]);

			for (int j = (matrix.length - 2); j >= 0; --j) {
				invertedMatrix[j][i] = auxiliaryMatrix[index[j]][i];

				for (int k = (j + 1); k < matrix.length; ++k) {
					invertedMatrix[j][i] -= (matrix[index[j]][k] * invertedMatrix[k][i]);
				}

				invertedMatrix[j][i] /= matrix[index[j]][j];
			}
		}

		return (invertedMatrix);
	}

	public static void transformToUpperTriangle (double[][] matrix, int[] index) {
		double[] c;
		double c0, c1, pi0, pi1, pj;
		int itmp, k;

		c = new double[matrix.length];

		for (int i = 0; i < matrix.length; ++i) {
			index[i] = i;
		}

		for (int i = 0; i < matrix.length; ++i) {
			c1 = 0;

			for (int j = 0; j < matrix.length; ++j) {
				c0 = Math.abs (matrix[i][j]);

				if (c0 > c1) {
					c1 = c0;
				}
			}

			c[i] = c1;
		}

		k = 0;

		for (int j = 0; j < (matrix.length - 1); ++j) {
			pi1 = 0;

			for (int i = j; i < matrix.length; ++i) {
				pi0 = Math.abs (matrix[index[i]][j]);
				pi0 /= c[index[i]];

				if (pi0 > pi1) {
					pi1 = pi0;
					k = i;
				}
			}

			itmp = index[j];
			index[j] = index[k];
			index[k] = itmp;

			for (int i = (j + 1); i < matrix.length; ++i) {
				pj = matrix[index[i]][j] / matrix[index[j]][j];
				matrix[index[i]][j] = pj;

				for (int l = (j + 1); l < matrix.length; ++l) {
					matrix[index[i]][l] -= pj * matrix[index[j]][l];
				}
			}
		}
	}

	/**
	 * Method that prints matrix
	 *
	 * @param matrix matrix to print
	 * @param id     what does the matrix contain?
	 */
	public static void printMatrix (int[][] matrix, int id) {
		double doubleMatrix[][] = new double[matrix.length][matrix[0].length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				doubleMatrix[i][j] = (double) matrix[i][j];
			}
		}

		printMatrix (doubleMatrix, id);
	}

	/**
	 * Method that prints matrix
	 *
	 * @param matrix matrix to print
	 * @param id     what does the matrix contain?
	 */
	public static void printMatrix (double[][] matrix, int id) {
		int cols, rows;

		rows = matrix.length;
		cols = matrix[0].length;

		switch (id) {
			case  1: Log.message(MessageFormat.format ("First matrix[{0}][{1}]:", rows, cols)); break;
			case  2: Log.message(MessageFormat.format ("Second matrix[{0}][{1}]:", rows, cols)); break;
			case  3: Log.message(MessageFormat.format ("Result[{0}][{1}]:", rows, cols)); break;
			case  4: Log.message(MessageFormat.format ("Inverted matrix[{0}][{1}]:", rows, cols)); break;
			default: Log.message(MessageFormat.format ("Matrix[{0}][{1}]:", rows, cols)); break;
		}

		String message = "";
		for (int i = 0; i < matrix.length; i++) {
			message+="[";

			for (int j = 0; j < matrix[i].length; j++) {
				message+=(matrix[i][j]);
				if ((j + 1) != matrix[i].length) {
					message+=(", ");
				}
			}

			if ((i + 1) != matrix.length) {
				message+=("]");
			} else {
				message+=("].");
			}
		}

		Log.message(message);
	}

	/**
	 * Build a "look at" matrix.  The X+ axis is pointing (to-from) normalized.
	 * The Z+ starts as pointing up.  Y+ is cross product of X and Z.  Z is then
	 * recalculated based on the correct X and Y.
	 * This will fail if to.z==from.z
	 *  
	 * @param from where i'm at
	 * @param to what I'm looking at
	 * @return
	 */
	public static Matrix4d lookAt(final Vector3d from, final Vector3d to) {
		Vector3d forward = new Vector3d();
		Vector3d left = new Vector3d();
		Vector3d up = new Vector3d(0,0,1);
		
		forward.sub(to,from);
		forward.normalize();
		left.cross(up, forward);
		left.normalize();
		up.cross(forward, left);
		up.normalize();

		Matrix4d lookAt = new Matrix4d(
				left.x,up.x,forward.x,0,
				left.y,up.y,forward.y,0,
				left.z,up.z,forward.z,0,
				0,0,0,1);
		
		return lookAt;
	}

	/**
	 * Build a "look at" matrix.  The X+ axis is pointing (to-from) normalized.
	 * The Z+ starts as pointing up.  Y+ is cross product of X and Z.  Z is then
	 * recalculated based on the correct X and Y.
	 * This will fail if to.z==from.z
	 *  
	 * @param from where i'm at
	 * @param to what I'm looking at
	 * @return
	 */
	public static Matrix4d lookAt(final Vector3d from, final Vector3d to,final Vector3d up) {
		Vector3d forward = new Vector3d();
		Vector3d left = new Vector3d();
		
		forward.sub(to,from);
		forward.normalize();
		left.cross(up, forward);
		left.normalize();
		up.cross(forward, left);
		up.normalize();

		Matrix4d lookAt = new Matrix4d(
				left.x,up.x,forward.x,0,
				left.y,up.y,forward.y,0,
				left.z,up.z,forward.z,0,
				0,0,0,1);
		
		return lookAt;
	}

	public static Vector3d getPosition(Matrix4d m) {	return new Vector3d(m.m03, m.m13, m.m23);	}
	public static Vector3d getXAxis(Matrix4d m) {	return new Vector3d(m.m00, m.m10, m.m20);	}
	public static Vector3d getYAxis(Matrix4d m) {	return new Vector3d(m.m01, m.m11, m.m21);	}
	public static Vector3d getZAxis(Matrix4d m) {	return new Vector3d(m.m02, m.m12, m.m22);	}

	public static void setPosition(Matrix4d m,Vector3d v) {	m.m03=v.x;  m.m13=v.y;  m.m23=v.z;	}
	public static void setXAxis(Matrix4d m,Vector3d v) {	m.m00=v.x;  m.m10=v.y;  m.m20=v.z;	}
	public static void setYAxis(Matrix4d m,Vector3d v) {	m.m01=v.x;  m.m11=v.y;  m.m21=v.z;	}
	public static void setZAxis(Matrix4d m,Vector3d v) {	m.m02=v.x;  m.m12=v.y;  m.m22=v.z;	}

	/**
	 * normalize the 3x3 component of the mTarget matrix.  Do not affect position. 
	 * @param mTarget the matrix that will be normalized.
	 */
	public static void normalize3(Matrix4d mTarget) {
		Matrix3d m3 = new Matrix3d();
		Vector3d v3 = new Vector3d();
		mTarget.get(v3);
		mTarget.get(m3);
		m3.normalize();
		mTarget.set(m3);
		mTarget.setTranslation(v3);
	}

	public static Matrix4d createIdentityMatrix4() {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		return m;
	}
}
