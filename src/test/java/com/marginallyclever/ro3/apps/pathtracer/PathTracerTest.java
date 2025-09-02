package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.ro3.apps.pathtracer.halton.HaltonWithMemory;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class PathTracerTest {
    @Test
    public void testUnitVectors() {
        HaltonWithMemory halton = new HaltonWithMemory();
        halton.resetMemory(0xDEADBEEFL);

        for(int i=0;i<100;i++) {
            // Generate a random unit vector
            Vector3d v = PathTracerHelper.getRandomUnitVector(halton);
            // Check if the vector is normalized (length should be 1)
            double length = v.length();
            assert Math.abs(length - 1.0) < 1e-6 : "Vector is not normalized: " + v;
        }
    }
}
