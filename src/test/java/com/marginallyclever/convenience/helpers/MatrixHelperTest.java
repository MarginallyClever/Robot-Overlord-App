package com.marginallyclever.convenience.helpers;

import com.marginallyclever.convenience.log.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertTrue(test);
            if (!test) {
                logger.info(i + "a=" + a);
                logger.info(i + "b=" + b);
                b.sub(a);
                logger.info(i + "d=" + b);
            }
            assertTrue(test);
        }
        logger.info("testEulerMatrix() OK");
    }
}
