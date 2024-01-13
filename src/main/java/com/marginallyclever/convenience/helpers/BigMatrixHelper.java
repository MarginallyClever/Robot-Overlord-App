package com.marginallyclever.convenience.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import java.text.MessageFormat;

public class BigMatrixHelper {
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
     * From <a href="https://www.sanfoundry.com/java-program-find-inverse-matrix/">...</a>
     *
     * @param a the matrix
     * @param index the pivoting order.
     */
    public static void gaussian(double[][] a, int[] index) {
        int n = index.length;
        double[] c = new double[n];

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
