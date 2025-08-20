package com.marginallyclever.ro3.apps.pathtracer;

import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;
import java.util.Random;
import java.util.SplittableRandom;

public class PathTracerTest {
    @Test
    public void testUnitVectors() {
        SplittableRandom random = new SplittableRandom();
        PathTracer pt = new PathTracer();
        for(int i=0;i<100;i++) {
            // Generate a random unit vector
            Vector3d v = PathTracerHelper.getRandomUnitVector(random);
            // Check if the vector is normalized (length should be 1)
            double length = v.length();
            assert Math.abs(length - 1.0) < 1e-6 : "Vector is not normalized: " + v;
        }
    }
}
