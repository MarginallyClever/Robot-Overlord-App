package com.marginallyclever.convenience.helpers;

import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Matrix4d;

public class BigMatrixHelperTest {
    @Test
    public void testTranspose() {
        double[][] matrix = {{1, 2}, {3, 4}};
        double[][] expected = {{1, 3}, {2, 4}};
        double[][] result = BigMatrixHelper.transpose(matrix);
        Assert.assertArrayEquals(expected, result);
    }

    @Test
    public void testDeterminant() {
        double[][] matrix = {{1, 2}, {3, 4}};
        double expected = -2;
        double result = BigMatrixHelper.determinant(matrix);
        Assert.assertEquals(expected, result, 0.0001);
    }

    @Test
    public void testMultiplyMatrices() {
        double[][] matrix1 = {{1, 2}, {3, 4}};
        double[][] matrix2 = {{5, 6}, {7, 8}};
        double[][] expected = {{19, 22}, {43, 50}};
        double[][] result = BigMatrixHelper.multiplyMatrices(matrix1, matrix2);
        Assert.assertArrayEquals(expected, result);
    }

    @Test
    public void testInvert() {
        double[][] matrix = {{4, 7}, {2, 6}};
        double[][] expected = {{0.6, -0.7}, {-0.2, 0.4}};
        double[][] result = BigMatrixHelper.invert(matrix);
        Assert.assertArrayEquals(expected, result);
    }


    @Test
    public void testMinor() {
        double[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        double[][] expectedMinor = {
                {1, 2},
                {7, 8}
        };

        // Testing the minor for row = 1, column = 2
        double[][] resultMinor = BigMatrixHelper.minor(matrix, 1, 2);

        for (int i = 0; i < expectedMinor.length; i++) {
            Assert.assertArrayEquals(expectedMinor[i], resultMinor[i], 0.001);
        }
    }

    @Test
    public void matrix4dToArray() {
        var matrix = new Matrix4d(
                1.0, 2.0, 3.0, 4.0,
                5.0, 6.0, 7.0, 8.0,
                9.0, 10.0, 11.0, 12.0,
                13.0, 14.0, 15.0, 16.0);
        double[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,14,15,16 };
        double[] result = BigMatrixHelper.matrix4dToArray(matrix);
        Assert.assertArrayEquals(expected, result, 0.001);
    }

    @Test
    public void testSingularValues() {
        // Identity matrix
        double[][] identity = {
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
        double[] sIdentity = BigMatrixHelper.singularValues(identity);
        Assert.assertArrayEquals(new double[]{1, 1, 1}, sIdentity, 1e-9);

        // Simple diagonal matrix
        double[][] diagonal = {
                {3, 0},
                {0, 2}
        };
        double[] sDiagonal = BigMatrixHelper.singularValues(diagonal);
        Assert.assertArrayEquals(new double[]{3, 2}, sDiagonal, 1e-9);

        // A matrix with known singular values
        // A = [1 1; 0 1]
        // A^T*A = [1 1; 1 2]
        // eigenvalues of A^T*A: (3 +/- sqrt(5))/2
        // singular values: sqrt((3 + sqrt(5))/2) and sqrt((3 - sqrt(5))/2)
        // ~ 1.618 and 0.618
        double[][] a = {{1, 1}, {0, 1}};
        double[] sA = BigMatrixHelper.singularValues(a);
        double s1 = Math.sqrt((3 + Math.sqrt(5)) / 2.0);
        double s2 = Math.sqrt((3 - Math.sqrt(5)) / 2.0);
        Assert.assertEquals(s1, sA[0], 1e-9);
        Assert.assertEquals(s2, sA[1], 1e-9);

        // Singular matrix
        double[][] singular = {{1, 1}, {1, 1}};
        double[] sSingular = BigMatrixHelper.singularValues(singular);
        Assert.assertEquals(Math.sqrt(4), sSingular[0], 1e-9);
        Assert.assertEquals(0.0, sSingular[1], 1e-9);
    }
}