package com.marginallyclever.convenience.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.text.MessageFormat;

public abstract class BigMatrixHelper {
    private static final Logger logger = LoggerFactory.getLogger(MatrixHelper.class);

    /**
     * invert an N*N matrix.
     * See <a href="https://github.com/rchen8/algorithms/blob/master/Matrix.java">...</a>
     *
     * @param a the matrix to invert.
     * @return the result.
     */
    public static double[][] invert(double[][] a) {
        double[][] inverse = new double[a.length][a.length];

        // minors and cofactors
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                double [][] m = minor(a, i, j);
                inverse[i][j] = Math.pow(-1, i + j)
                        * determinant(m);
            }
        }

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

    public static double [][] transpose(double [][] matrix) {
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
    public static double determinant(double[][] matrix) {
        if( matrix.length==1) {
            return matrix[0][0];
        }
        if (matrix.length != matrix[0].length)
            throw new IllegalStateException("invalid dimensions");
        if (matrix.length == 2)
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        double det = 0;
        for (int i = 0; i < matrix[0].length; i++) {
            double [][] m = minor(matrix, 0, i);
            det += Math.pow(-1, i) * matrix[0][i] * determinant(m);
        }
        return det;
    }

    /**
     * Method that creates minor of given matrix
     * @param matrix matrix of which we need to create minor
     * @param row row of element to exclude
     * @param column column of element to exclude
     * @return minor of given matrix
     */
    public static double[][] minor(double[][] matrix, int row, int column) {
        double[][] minor = new double[matrix.length - 1][matrix.length - 1];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (i != row && j != column) {
                    minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
                }
            }
        }
        return minor;
    }

    /**
     * Method that multiplies two matrices and returns the result
     * @param x first matrix
     * @param y second matrix
     * @return result after multiplication
     */
    public static double[][] multiplyMatrices (double[][] x, double[][] y) {
        double[][] result;
        int xColumns, xRows, yColumns, yRows;

        xRows = x.length;
        xColumns = x[0].length;
        yRows = y.length;
        yColumns = y[0].length;
        result = new double[xRows][yColumns];

        if (xColumns != yRows) {
            throw new IllegalArgumentException ("Matrices don't match: "+xColumns+" != "+yRows);
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
     * matrix-vector multiplication (y = A * x)
     * @param a matrix
     * @param x vector
     * @return result of multiplication
     */
    public static double[] multiply(double[][] a, double[] x) {
        int m = a.length;
        int n = a[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                y[i] += a[i][j] * x[j];
            }
        }
        return y;
    }

    /**
     * Method that prints matrix
     * @param matrix matrix to print
     */
    public static void printMatrix (double[][] matrix) {
        int cols, rows;

        rows = matrix.length;
        cols = matrix[0].length;

        StringBuilder message = new StringBuilder();
        message.append(MessageFormat.format("Matrix[{0}][{1}]:", rows, cols));

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

    public static double[][] createMatrix(int rows, int cols) {
        double [][] m = new double[rows][];
        for(int i=0;i<rows;++i) {
            m[i]=new double[cols];
        }
        return m;
    }

    /**
     * Convert a {@link javax.vecmath.Matrix4d} to an array of doubles.  {@link Matrix4d} is row-major and
     * OpenGL is column-major.
     * @param m the matrix to convert
     * @return a double array of length 16
     */
    public static double [] matrix4dToArray(Matrix4d m) {
        return new double[] {
            m.m00, m.m01, m.m02, m.m03,
            m.m10, m.m11, m.m12, m.m13,
            m.m20, m.m21, m.m22, m.m23,
            m.m30, m.m31, m.m32, m.m33,
        };
    }

    /**
     * Compute the singular values of a matrix using the Jacobi SVD algorithm.
     * This is a simple implementation for small matrices.
     * @param matrix the matrix to decompose
     * @return an array of singular values in descending order
     */
    public static double[] singularValues(double[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        // We compute SVD of A by computing eigenvalues of A^T * A (if m >= n) or A * A^T (if m < n)
        // or we can use a direct Jacobi SVD on A itself.
        // For simplicity and since it's 6x6, let's use the one-sided Jacobi SVD algorithm.
        
        double[][] A = new double[m][n];
        for (int i = 0; i < m; i++) System.arraycopy(matrix[i], 0, A[i], 0, n);

        int maxSweep = 100;
        double eps = 1e-12;
        
        // One-sided Jacobi SVD
        for (int sweep = 0; sweep < maxSweep; sweep++) {
            double maxErr = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double aii = 0, ajj = 0, aij = 0;
                    for (int k = 0; k < m; k++) {
                        aii += A[k][i] * A[k][i];
                        ajj += A[k][j] * A[k][j];
                        aij += A[k][i] * A[k][j];
                    }
                    
                    maxErr = Math.max(maxErr, Math.abs(aij) / Math.sqrt(aii * ajj));
                    
                    if (Math.abs(aij) > eps) {
                        double tau = (ajj - aii) / (2 * aij);
                        double t = Math.signum(tau) / (Math.abs(tau) + Math.sqrt(1 + tau * tau));
                        double c = 1 / Math.sqrt(1 + t * t);
                        double s = c * t;
                        
                        for (int k = 0; k < m; k++) {
                            double aki = A[k][i];
                            double akj = A[k][j];
                            A[k][i] = c * aki - s * akj;
                            A[k][j] = s * aki + c * akj;
                        }
                    }
                }
            }
            if (maxErr < eps) break;
        }
        
        double[] s = new double[n];
        for (int j = 0; j < n; j++) {
            double norm = 0;
            for (int i = 0; i < m; i++) norm += A[i][j] * A[i][j];
            s[j] = Math.sqrt(norm);
        }
        
        // Sort in descending order
        java.util.Arrays.sort(s);
        for (int i = 0; i < s.length / 2; i++) {
            double tmp = s[i];
            s[i] = s[s.length - 1 - i];
            s[s.length - 1 - i] = tmp;
        }
        
        return s;
    }
}
