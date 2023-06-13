package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Plane;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.*;
import java.nio.FloatBuffer;
import java.text.MessageFormat;

/**
 * Convenience methods for matrixes
 * @author aggra
 *
 */
public class MatrixHelper {
	private static final Logger logger = LoggerFactory.getLogger(MatrixHelper.class);

	/**
	 * See drawMatrix(gl,p,u,v,w,1)
	 *
	 * @param m     matrix to draw
	 * @param scale scale to draw at
	 */
	static public Mesh drawMatrix(Matrix4d m, float scale) {
		float x = (float)m.m03;
		float y = (float)m.m13;
		float z = (float)m.m23;

		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_LINES);
		mesh.addColor(1,0,0,1);		mesh.addVertex(x, y, z);
		mesh.addColor(1,0,0,1);		mesh.addVertex(x+(float)m.m00*scale, y+(float)m.m10*scale, z+(float)m.m20*scale);
		mesh.addColor(0,1,0,1);		mesh.addVertex(x, y, z);
		mesh.addColor(0,1,0,1);		mesh.addVertex(x+(float)m.m01*scale, y+(float)m.m11*scale, z+(float)m.m21*scale);
		mesh.addColor(0,0,1,1);		mesh.addVertex(x, y, z);
		mesh.addColor(0,0,1,1);		mesh.addVertex(x+(float)m.m02*scale, y+(float)m.m12*scale, z+(float)m.m22*scale);
		return mesh;
	}

	static public Mesh drawMatrix(Tuple3d p,float scale) {
		Matrix4d m = MatrixHelper.createIdentityMatrix4();
		m.setTranslation(new Vector3d(p.x,p.y,p.z));
		return MatrixHelper.drawMatrix(m,scale);
	}

	static public Mesh drawMatrix(float scale) {
		return drawMatrix(new Vector3d(),scale);
	}

	/**
	 * See drawMatrix(gl,p,u,v,w,1)
	 */
	static public void drawMatrix(GL3 gl,Vector3d p,Vector3d u,Vector3d v,Vector3d w) {
		drawMatrix(gl,p,u,v,w,1);
	}
	
	/**
	 * Draw the three vectors of a matrix at a point
	 * @param gl render context
	 * @param p position at which to draw
	 * @param u in yellow (1,1,0)
	 * @param v in teal (0,1,1)
	 * @param w in magenta (1,0,1)
	 * @param scale nominally 1
	 */
	static public void drawMatrix(GL3 gl,Vector3d p,Vector3d u,Vector3d v,Vector3d w,float scale) {
		Matrix4d m = new Matrix4d(
				u.x,u.y,u.z,p.x,
				v.x,v.y,v.z,p.y,
				w.x,w.y,w.z,p.z,
				0,0,0,1.0
				);
		drawMatrix(m,scale);
	}

	/**
	 * Confirms that this matrix is a rotation matrix.  Matrix A * transpose(A) should be the Identity.
	 * See also <a href="https://www.learnopencv.com/rotation-matrix-to-euler-angles/">...</a>
	 * Eulers are using the ZYX convention.
	 * @param mat the {@link Matrix3d} to check.
	 * @return true if this is a rotation matrix.
	 */
	static public boolean isRotationMatrix(Matrix3d mat) {
		Matrix3d m1 = new Matrix3d(mat);
		Matrix3d m2 = new Matrix3d();
		m2.transpose(m1);
		m1.mul(m2);
		m2.setIdentity();
		return m1.epsilonEquals(m2, 1e-6);
	}
	
	/**
	 * Convert a matrix to Euler rotations.  There are many valid solutions.
	 * See also <a href="https://www.learnopencv.com/rotation-matrix-to-euler-angles/">learnOpenCV</a>
	 * Eulers are using the ZYX convention.
	 * @param mat the matrix to convert.
	 * @return resulting radian rotations.  One possible solution.
	 */
	static public Vector3d matrixToEuler(Matrix3d mat) {
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
	 * @param mat the Matrix4d to convert.
	 * @return a valid Euler solution to the matrix.
	 */
	static public Vector3d matrixToEuler(Matrix4d mat) {
		Matrix3d m3 = new Matrix3d();
		mat.get(m3);
		return matrixToEuler(m3);
	}
	
	/**
	 * Convert Euler rotations to a matrix.
	 * See also <a href="https://www.learnopencv.com/rotation-matrix-to-euler-angles/">...</a>
	 * Eulers are using the ZYX convention.
	 * @param v radian rotation values
	 * @return Matrix3d resulting matrix
	 */
	static public Matrix3d eulerToMatrix(Vector3d v) {
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
	static public boolean interpolate(final Matrix4d start,final Matrix4d end,double alpha,Matrix4d result) {
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

	/**
	 * invert an N*N matrix.
	 * See https://github.com/rchen8/algorithms/blob/master/Matrix.java
	 * 
	 * @param a the matrix to invert.
	 * @return the result.
	 */
	static public double[][] invert(double a[][]) {
		double[][] inverse = new double[a.length][a.length];

		// minors and cofactors
		for (int i = 0; i < a.length; i++)
			for (int j = 0; j < a[i].length; j++)
				inverse[i][j] = Math.pow(-1, i + j)
						* determinant(minor(a, i, j));

		// transpose and divide by determinant
		double det = determinant(a);
		inverse = transpose(inverse);
		for (int i = 0; i < inverse.length; i++) {
			for (int j = 0; j < inverse[i].length; j++) {
				inverse[i][j] /= det;
			}
		}


		return inverse;
	}
	
	static public double [][] transpose(double [][] matrix) {
		int h = matrix.length;
		int w = matrix[0].length;
		
		double [][] transposedMatrix = createMatrix(w,h);
	
		for(int y=0;y<h;y++) {
			for(int x=0;x<w;x++) {
				transposedMatrix[x][y] = matrix[y][x];
			}
		}
	
		return transposedMatrix;
	}

	/**
	 * Method that calculates determinant of given matrix
	 *
	 * @param matrix matrix of which we need to know determinant
	 *
	 * @return determinant of given matrix
	 */
	static public double matrixDeterminant (double[][] matrix) {
		double [][] temporary;
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

	static public double determinant(double[][] matrix) {
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
				if (i != row && j != column)
					minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
		return minor;
	}
	
	/**
	 * Method to carry out the partial-pivoting Gaussian elimination.
	 * From <a href="https://www.sanfoundry.com/java-program-find-inverse-matrix/">...</a>
	 *
	 * @param a the matrix
	 * @param index the pivoting order.
	 */
	static public void gaussian(double a[][], int index[]) {
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
	 * @param x first matrix
	 * @param y second matrix
	 * @return result after multiplication
	 */
	static public double[][] multiplyMatrices (double[][] x, double[][] y) {
		double[][] result;
		int xColumns, xRows, yColumns, yRows;

		xRows = x.length;
		xColumns = x[0].length;
		yRows = y.length;
		yColumns = y[0].length;
		result = new double[xRows][yColumns];

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

    // matrix-vector multiplication (y = A * x)
    static public double[] multiply(double[][] a, double[] x) {
        int m = a.length;
        int n = a[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += a[i][j] * x[j];
        return y;
    }
    
	static public double[][] invertMatrix (double[][] matrix) {
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

	static public void transformToUpperTriangle (double[][] matrix, int[] index) {
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
	static public void printMatrix (int[][] matrix, int id) {
		double [][] doubleMatrix = new double[matrix.length][matrix[0].length];

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
	static public void printMatrix (double[][] matrix, int id) {
		int cols, rows;

		rows = matrix.length;
		cols = matrix[0].length;

		switch (id) {
			case 1 -> logger.info(MessageFormat.format("First matrix[{0}][{1}]:", rows, cols));
			case 2 -> logger.info(MessageFormat.format("Second matrix[{0}][{1}]:", rows, cols));
			case 3 -> logger.info(MessageFormat.format("Result[{0}][{1}]:", rows, cols));
			case 4 -> logger.info(MessageFormat.format("Inverted matrix[{0}][{1}]:", rows, cols));
			default -> logger.info(MessageFormat.format("Matrix[{0}][{1}]:", rows, cols));
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < matrix.length; i++) {
			message.append("[");

			for (int j = 0; j < matrix[i].length; j++) {
				message.append(matrix[i][j]);
				if ((j + 1) != matrix[i].length) {
					message.append(", ");
				}
			}

			if ((i + 1) != matrix.length) {
				message.append("]");
			} else {
				message.append("].");
			}
		}

		logger.info(message.toString());
	}

	/**
	 * Build a "look at" matrix.  The X+ axis is pointing (to-from) normalized.
	 * The Z+ starts as pointing up.  Y+ is cross product of X and Z.  Z is then
	 * recalculated based on the correct X and Y.
	 * This will fail if to-from is parallel to up.
	 *  
	 * @param from where i'm at
	 * @param to what I'm looking at
	 * @return a matrix that will transform a point to the "look at" orientation
	 */
	static public Matrix3d lookAt(final Vector3d from, final Vector3d to) {
		Vector3d forward = new Vector3d();
		Vector3d left = new Vector3d();
		Vector3d up = new Vector3d();
		
		forward.sub(to,from);
		forward.normalize();
		if(forward.z==1) {
			up.set(0,1,0);
		} else if(forward.z==-1) {
			up.set(0,-1,0);
		} else {
			up.set(0,0,1);
		}
		left.cross(up, forward);
		left.normalize();
		up.cross(forward, left);
		up.normalize();

		return new Matrix3d(
				left.x,up.x,forward.x,
				left.y,up.y,forward.y,
				left.z,up.z,forward.z);
	}

	/**
	 * Build a "look at" matrix.  The X+ axis is pointing (to-from) normalized.
	 * The Z+ starts as pointing up.  Y+ is cross product of X and Z.  Z is then
	 * recalculated based on the correct X and Y.
	 * This will fail if to-from is parallel to up.
	 *  
	 * @param from where i'm at
	 * @param to what I'm looking at
	 * @return a matrix that will transform a point to the "look at" orientation
	 */
	static public Matrix4d lookAt(final Vector3d from, final Vector3d to,final Vector3d up) {
		Vector3d forward = new Vector3d();
		Vector3d left = new Vector3d();
		
		forward.sub(to,from);
		forward.normalize();
		left.cross(up, forward);
		left.normalize();
		up.cross(forward, left);
		up.normalize();

		Matrix4d lookAt = new Matrix4d(
				left.x,up.x,forward.x,from.x,
				left.y,up.y,forward.y,from.y,
				left.z,up.z,forward.z,from.z,
				0,0,0,1);
		
		return lookAt;
	}

	static public Vector3d getXAxis(Matrix4d m) {		return new Vector3d(m.m00, m.m10, m.m20);	}
	static public Vector3d getYAxis(Matrix4d m) {		return new Vector3d(m.m01, m.m11, m.m21);	}
	static public Vector3d getZAxis(Matrix4d m) {		return new Vector3d(m.m02, m.m12, m.m22);	}
	static public Vector3d getPosition(Matrix4d m) {	return new Vector3d(m.m03, m.m13, m.m23);	}

	static public void setXAxis(Matrix4d m,Vector3d v) {	m.m00=v.x;  m.m10=v.y;  m.m20=v.z;	}
	static public void setYAxis(Matrix4d m,Vector3d v) {	m.m01=v.x;  m.m11=v.y;  m.m21=v.z;	}
	static public void setZAxis(Matrix4d m,Vector3d v) {	m.m02=v.x;  m.m12=v.y;  m.m22=v.z;	}
	static public void setPosition(Matrix4d m,Vector3d v) {	m.m03=v.x;  m.m13=v.y;  m.m23=v.z;	}

	/**
	 * normalize the 3x3 component of the mTarget matrix.  Do not affect position. 
	 * @param mTarget the matrix that will be normalized.
	 */
	static public void normalize3(Matrix4d mTarget) {
		Matrix3d m3 = new Matrix3d();
		Vector3d v3 = new Vector3d();
		mTarget.get(v3);
		mTarget.get(m3);
		m3.normalize();
		mTarget.set(m3);
		mTarget.setTranslation(v3);
	}

	static public Matrix4d createIdentityMatrix4() {
		return new Matrix4d(
				1,0,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1);
	}
	
	// see https://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_angle
	static public Matrix3d getMatrixFromAxisAndRotation(Vector3d axis,double degrees) {
		Matrix3d m = new Matrix3d();
		
		double radians = Math.toRadians(degrees);
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		double oneMinusC = 1-c;
		double x = axis.x;
		double y = axis.y;
		double z = axis.z;
		
		double xzc = x*z*oneMinusC;
		double yzc = y*z*oneMinusC;
		double xyc = x*y*oneMinusC;
		
		m.m00 = c + x*x*oneMinusC;
		m.m01 = xyc - z*s;
		m.m02 = xzc + y*s;

		m.m10 = xyc +z*s;
		m.m11 = c + y*y*oneMinusC;
		m.m12 = yzc - x*s;
		
		m.m20 = xzc - y*s;
		m.m21 = yzc + x*s;
		m.m22 = c + z*z*oneMinusC;
			
		return m;
	}

	/**
	 * returns Q and D such that Diagonal matrix D = QT * A * Q;  and  A = Q*D*QT
	 * see https://en.wikipedia.org/wiki/Jacobi_eigenvalue_algorithm
	 * see https://en.wikipedia.org/wiki/Diagonalizable_matrix#Diagonalization
	 * @param a a symmetric matrix.
	 * @param dOut where to store the results
	 * @param qOut where to store the results
	 */

	static public void diagonalize(Matrix3d a,Matrix3d dOut,Matrix3d qOut) {
		Matrix3d d = new Matrix3d();
		d.setIdentity();
		Matrix3d q = new Matrix3d(a);

		double offMatrixNorm2 = q.m01*q.m01 + q.m02*q.m02 + q.m12*q.m12;

		final int ite_max = 1024;
		int ite = 0;
		while (offMatrixNorm2 > 1e-6 && ite++ < ite_max) {
			double e01 = q.m01 * q.m01;
			double e02 = q.m02 * q.m02;
			double e12 = q.m12 * q.m12;
			// Find the pivot element
			int i, j;
			if (e01 > e02) {
				if (e12 > e01) {
					i = 1;
					j = 2;
				} else {
					i = 0;
					j = 1;
				}
			} else {
				if (e12 > e02) {
					i = 1;
					j = 2;
				} else {
					i = 0;
					j = 2;
				}
			}

			// Compute the rotation angle
			double angle;
			if(Math.abs(q.getElement(j,j) - q.getElement(i,i)) < 1e-6) {
				angle = Math.PI / 4.0;
			} else {
				angle = 0.5 * Math.atan(2 * q.getElement(i,j) / (q.getElement(j,j) - q.getElement(i,i)));
			}

			// Compute the rotation matrix
			Matrix3d rot = new Matrix3d();
			rot.setIdentity();
			double c = Math.cos(angle);
			double s = Math.sin(angle);
			rot.setElement(i,i, c);		rot.setElement(i,j,-s); 
			rot.setElement(j,i, s);		rot.setElement(j,j, c);

			// Apply the rotation
			//*this = rot * *this * rot.transposed();
			Matrix3d rt = new Matrix3d();
			rt.transpose(rot);
			Matrix3d temp = new Matrix3d();
			temp.mul(rot,q);
			q.mul(temp,rt);

			// Update the off matrix norm
			offMatrixNorm2 -= q.getElement(i,j) * q.getElement(i,j);

			d.mul(rot);
		}

		dOut.set(d);
		qOut.set(q);
	}

	/**
	 * Use Quaternions to estimate the distance between two matrixes (both linear and rotational).
	 * @param mStart matrix of start pose
	 * @param mEnd matrix of end pose
	 * @return 6 doubles that will be filled with the XYZ translation and RPY rotation.
	 */
	static public double[] getCartesianBetweenTwoMatrices(final Matrix4d mStart, final Matrix4d mEnd) {
		// get the linear movement
		Vector3d diff = new Vector3d(
				mEnd.m03-mStart.m03,
				mEnd.m13-mStart.m13,
				mEnd.m23-mStart.m23);
		// get the quaternions
		Quat4d qStart = new Quat4d();
		Quat4d qEnd = new Quat4d();
		qStart.set(mStart);
		qEnd.set(mEnd);
		// then get the difference in quaternions.  diff * qStart = qEnd --> diff = qEnd * invert(qStart)
		Quat4d qDiff = new Quat4d();
		qDiff.mulInverse(qEnd,qStart);
		// get the radian roll/pitch/yaw
		double [] rpy = MathHelper.quatToEuler(qDiff);
		
		return new double[] { diff.x,diff.y,diff.z, -rpy[0],-rpy[1],-rpy[2] };
	}

	public static double[][] createMatrix(int rows, int cols) {
		double [][] m = new double[rows][];
		for(int i=0;i<rows;++i) {
			m[i]=new double[cols];
		}
		return m;
	}

	/**
	 * Convert a {@link Matrix3d} to an array of doubles.  Matrix4d and OpenGL are column-major.
	 * @param m the matrix to convert
	 * @return a double array of length 9
	 */
	public static double [] matrix3dToArray(Matrix3d m) {
		return new double[] {
			m.m00,
			m.m10,
			m.m20,
			m.m01,
			m.m11,
			m.m21,
			m.m02,
			m.m12,
			m.m22
		};
	}

	/**
	 * Convert a {@link javax.vecmath.Matrix4d} to an array of doubles.  Matrix4d and OpenGL are column-major.
	 * @param m the matrix to convert
	 * @return a double array of length 16
	 */
	public static double [] matrix4dToArray(Matrix4d m) {
		return new double[] {
			m.m00,
			m.m10,
			m.m20,
			m.m30,
			m.m01,
			m.m11,
			m.m21,
			m.m31,
			m.m02,
			m.m12,
			m.m22,
			m.m32,
			m.m03,
			m.m13,
			m.m23,
			m.m33,
		};
	}

	public static FloatBuffer matrixToFloatBuffer(Matrix4d m) {
		FloatBuffer matrixBuffer = FloatBuffer.allocate(16);
		matrixBuffer.put( (float)m.m00 );
		matrixBuffer.put( (float)m.m01 );
		matrixBuffer.put( (float)m.m02 );
		matrixBuffer.put( (float)m.m03 );

		matrixBuffer.put( (float)m.m10 );
		matrixBuffer.put( (float)m.m11 );
		matrixBuffer.put( (float)m.m12 );
		matrixBuffer.put( (float)m.m13 );

		matrixBuffer.put( (float)m.m20 );
		matrixBuffer.put( (float)m.m21 );
		matrixBuffer.put( (float)m.m22 );
		matrixBuffer.put( (float)m.m23 );

		matrixBuffer.put( (float)m.m30 );
		matrixBuffer.put( (float)m.m31 );
		matrixBuffer.put( (float)m.m32 );
		matrixBuffer.put( (float)m.m33 );
		matrixBuffer.rewind();

		return matrixBuffer;
	}

	public static float [] matrixToArrayF(Matrix4d m) {
		float [] list = new float[16];

		list[0] = (float)m.m00;
		list[1] = (float)m.m01;
		list[2] = (float)m.m02;
		list[3] = (float)m.m03;
		list[4] = (float)m.m10;
		list[5] = (float)m.m11;
		list[6] = (float)m.m12;
		list[7] = (float)m.m13;
		list[8] = (float)m.m20;
		list[9] = (float)m.m21;
		list[10] = (float)m.m22;
		list[11] = (float)m.m23;
		list[12] = (float)m.m30;
		list[13] = (float)m.m31;
		list[14] = (float)m.m32;
		list[15] = (float)m.m33;

		return list;
	}

	/**
	 *
	 * @param gl render context
	 * @param type either GL3.GL_MODELVIEW_MATRIX or GL3.GL_PROJECTION_MATRIX
	 * @return
	 */
	public static Matrix4d getMatrix(GL3 gl, int type) {
		Matrix4d m = new Matrix4d();
		double [] list = new double[16];
		gl.glGetDoublev(type, list,0);
		m.set(list);
		return m;
	}

	public static Plane getXYPlane(Matrix4d pivot) {
		return new Plane(
				getPosition(pivot),
				getZAxis(pivot)
		);
	}

	public static Plane getXZPlane(Matrix4d pivot) {
		return new Plane(
				getPosition(pivot),
				getYAxis(pivot)
		);
	}

	public static Plane getYZPlane(Matrix4d pivot) {
		return new Plane(
				getPosition(pivot),
				getXAxis(pivot)
		);
	}

	public static Matrix4d orthographicMatrix4d(double left, double right, double bottom, double top, double near, double far) {
		double [] list = new double[16];
		org.joml.Matrix4d ortho = new org.joml.Matrix4d();
		ortho.setOrtho(left, right, bottom, top, near, far).get(list);
		return new Matrix4d(list);
	}

	public static Matrix4d perspectiveMatrix4d(double fovY, double aspect, double near, double far) {
		double [] list = new double[16];
		org.joml.Matrix4d perspective = new org.joml.Matrix4d();
		perspective.setPerspective(Math.toRadians(fovY), aspect, near, far).get(list);
		return new Matrix4d(list);
	}

	public static Matrix4d createFrustum(double left, double right, double bottom, double top, double near, double far) {
		org.joml.Matrix4d frustum = new org.joml.Matrix4d();
		frustum.frustum(left,right,bottom,top, near, far);
		double [] list = new double[16];
		frustum.get(list);
		return new Matrix4d(list);
	}

	public static Vector3d transform(Matrix4d mat, Vector3d vector) {
		Vector3d force = new Vector3d(vector);
		mat.transform(force);
		return force;
	}
}
