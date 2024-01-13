package com.marginallyclever.convenience.helpers;

import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Matrix3d;
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

    
}