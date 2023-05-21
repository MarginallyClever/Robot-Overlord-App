package com.marginallyclever.convenience.helpers;

import com.marginallyclever.convenience.helpers.StringHelper;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringHelperTest {
    @Test
    public void testDoubleToBytes() {
        assertArrayEquals(new byte[]{63, -116, -52, -51}, StringHelper.floatToByteArray(1.1f));
    }

    @Test
    public void testBytesToDouble() {
        assertEquals(1.1f, StringHelper.byteArrayToFloat(new byte[]{63, -116, -52, -51}), 0);
    }

    @Test
    public void testTuple3d() throws Exception {
        Vector3d v1 = new Vector3d(Math.random(), Math.random(), Math.random());
        String s = v1.toString();
        Vector3d v2 = (Vector3d) StringHelper.parseTuple3d(s);
        assertEquals(v1.x, v2.x, 0);
        assertEquals(v1.y, v2.y, 0);
        assertEquals(v1.z, v2.z, 0);
    }

    @Test
    public void testMatrix3d() throws Exception {
        double[] m = new double[9];
        for (int i = 0; i < m.length; ++i) m[i] = Math.random();
        Matrix3d v1 = new Matrix3d(m);
        String s = v1.toString();
        Matrix3d v2 = StringHelper.parseMatrix3d(s);
        assertEquals(v1.m00, v2.m00, 0);
        assertEquals(v1.m01, v2.m01, 0);
        assertEquals(v1.m02, v2.m02, 0);

        assertEquals(v1.m10, v2.m10, 0);
        assertEquals(v1.m11, v2.m11, 0);
        assertEquals(v1.m12, v2.m12, 0);

        assertEquals(v1.m20, v2.m20, 0);
        assertEquals(v1.m21, v2.m21, 0);
        assertEquals(v1.m22, v2.m22, 0);
    }

    @Test
    public void testMatrix4d() throws Exception {
        double[] m = new double[16];
        for (int i = 0; i < m.length; ++i) m[i] = Math.random();
        Matrix4d v1 = new Matrix4d(m);
        String s = v1.toString();
        Matrix4d v2 = StringHelper.parseMatrix4d(s);
        assertEquals(v1.m00, v2.m00, 0);
        assertEquals(v1.m01, v2.m01, 0);
        assertEquals(v1.m02, v2.m02, 0);
        assertEquals(v1.m03, v2.m03, 0);

        assertEquals(v1.m10, v2.m10, 0);
        assertEquals(v1.m11, v2.m11, 0);
        assertEquals(v1.m12, v2.m12, 0);
        assertEquals(v1.m13, v2.m13, 0);

        assertEquals(v1.m20, v2.m20, 0);
        assertEquals(v1.m21, v2.m21, 0);
        assertEquals(v1.m22, v2.m22, 0);
        assertEquals(v1.m23, v2.m23, 0);

        assertEquals(v1.m30, v2.m30, 0);
        assertEquals(v1.m31, v2.m31, 0);
        assertEquals(v1.m32, v2.m32, 0);
        assertEquals(v1.m33, v2.m33, 0);
    }
}
