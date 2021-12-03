package com.marginallyclever.robotOverlord;

import com.marginallyclever.convenience.MatrixHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatrixHelperTest {
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
                System.out.println(i + "a=" + a);
                System.out.println(i + "b=" + b);
                b.sub(a);
                System.out.println(i + "d=" + b);
            }
            assertTrue(test);
        }
        System.out.println("testEulerMatrix() OK");
    }
}
