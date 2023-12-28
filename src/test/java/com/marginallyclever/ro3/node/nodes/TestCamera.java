package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class TestCamera {
    @Test
    public void testPanTiltInverses() {
        Registry.start();
        Camera camera = Registry.getActiveCamera();

        for (int pan = -180; pan <= 180; pan += 10) {
            for (int tilt = -90; tilt <= 90; tilt += 10) {
                double[] before = new double[]{pan, tilt};
                Matrix3d panTiltMatrix = camera.buildPanTiltMatrix(before);
                Matrix4d matrix = new Matrix4d(panTiltMatrix, new Vector3d(), 1.0);
                double[] after = camera.getPanTiltFromMatrix(matrix);

                System.out.println("before="+before[0]+","+before[1]+" after="+after[0]+","+after[1]);
                Assertions.assertArrayEquals(before, after, 0.01);
            }
        }
    }
}
