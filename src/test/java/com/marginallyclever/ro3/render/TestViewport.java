package com.marginallyclever.ro3.render;

import com.marginallyclever.ro3.node.nodes.Camera;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class TestViewport {
    @Test
    public void testPanTiltInverses() {
        Viewport viewport = new Viewport();
        for (int pan = -180; pan <= 180; pan += 10) {
            for (int tilt = -90; tilt <= 90; tilt += 10) {
                double[] before = new double[]{pan, tilt};
                Matrix3d panTiltMatrix = viewport.buildPanTiltMatrix(before);
                Matrix4d matrix = new Matrix4d(panTiltMatrix, new Vector3d(), 1.0);
                double[] after = viewport.getPanTiltFromMatrix(matrix);

                System.out.println("before="+before[0]+","+before[1]+" after="+after[0]+","+after[1]);
                Assertions.assertArrayEquals(before, after, 0.01);
            }
        }
    }
}
