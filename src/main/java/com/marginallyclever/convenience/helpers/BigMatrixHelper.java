package com.marginallyclever.convenience.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
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
}
