package com.marginallyclever.convenience.helpers;

import com.marginallyclever.convenience.log.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;


public class MatrixHelperTest {
    private static final Logger logger = LoggerFactory.getLogger(MatrixHelperTest.class);
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    @Test
    public void testEulerMatrix() {
        Vector3d v1 = new Vector3d();

        for (int i = 0; i < 100; ++i) {
            v1.x = Math.random() * Math.PI * 2.0;
            v1.y = Math.random() * Math.PI * 2.0;
            v1.z = Math.random() * Math.PI * 2.0;

            Matrix3d a = MatrixHelper.eulerToMatrix(v1);
            Vector3d v2 = MatrixHelper.matrixToEuler(a);
            Matrix3d b = MatrixHelper.eulerToMatrix(v2);

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
}