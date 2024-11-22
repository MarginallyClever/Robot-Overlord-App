package com.marginallyclever.convenience.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;


public class MatrixHelperTest {
    private static final Logger logger = LoggerFactory.getLogger(MatrixHelperTest.class);

    @Test
    public void testEulerMatrix() {
        Vector3d v1 = new Vector3d();

        for (int i = 0; i < 100; ++i) {
            v1.x = Math.random() * Math.PI * 2.0;
            v1.y = Math.random() * Math.PI * 2.0;
            v1.z = Math.random() * Math.PI * 2.0;

            Matrix3d a = MatrixHelper.eulerToMatrix(v1, MatrixHelper.EulerSequence.YXZ);
            Vector3d v2 = MatrixHelper.matrixToEuler(a, MatrixHelper.EulerSequence.YXZ);
            Matrix3d b = MatrixHelper.eulerToMatrix(v2, MatrixHelper.EulerSequence.YXZ);

            boolean test = b.epsilonEquals(a, 1e-6);
            if (!test) {
                logger.info(i + "a=" + a);
                logger.info(i + "b=" + b);
                b.sub(a);
                logger.info(i + "d=" + b);
            }
            Assertions.assertTrue(test);
        }
        logger.info("testEulerMatrix() OK");
    }

    // TODO fix euler to matrix so this test passes!
    @Disabled
    @Test
    public void eulerToMatrix() {
        Vector3d v1 = new Vector3d();
        v1.x = Math.toRadians(15);
        v1.y = Math.toRadians(25);
        v1.z = Math.toRadians(35);

        for(MatrixHelper.EulerSequence s : MatrixHelper.EulerSequence.values()) {
            Matrix3d a = MatrixHelper.eulerToMatrix(v1,s);
            Vector3d v2 = MatrixHelper.matrixToEuler(a,s);
            Matrix3d b = MatrixHelper.eulerToMatrix(v2,s);

            // Check if the original and resulting rotation matrices are approximately equal
            System.out.println(s+" a="+a+" b="+b);
            Assertions.assertTrue(a.epsilonEquals(b, 1e-3));
        }
    }

    @Test
    public void testScaleMatrix() {
        Matrix4d a = MatrixHelper.createScaleMatrix4(4);
        Assertions.assertEquals(4,a.m00);
        Assertions.assertEquals(4,a.m11);
        Assertions.assertEquals(4,a.m22);
        Assertions.assertEquals(1,a.m33);
    }

    @Test
    public void lookAt() {
        var m3 = MatrixHelper.lookAt(new Vector3d(1,0,0),new Vector3d(0,0,0));
        var m4 = new Matrix4d(m3,new Vector3d(),1);
        var z = MatrixHelper.getZAxis(m4);
        Assertions.assertEquals(-1,z.x);

        m3 = MatrixHelper.lookAt(new Vector3d(-1,0,0),new Vector3d(0,0,0));
        m4 = new Matrix4d(m3,new Vector3d(),1);
        z = MatrixHelper.getZAxis(m4);
        Assertions.assertEquals(1,z.x);

        m3 = MatrixHelper.lookAt(new Vector3d(0,1,0),new Vector3d(0,0,0));
        m4 = new Matrix4d(m3,new Vector3d(),1);
        z = MatrixHelper.getZAxis(m4);
        Assertions.assertEquals(-1,z.y);

        m3 = MatrixHelper.lookAt(new Vector3d(0,-1,0),new Vector3d(0,0,0));
        m4 = new Matrix4d(m3,new Vector3d(),1);
        z = MatrixHelper.getZAxis(m4);
        Assertions.assertEquals(1,z.y);
    }
}