package com.marginallyclever.convenience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class RayTest {
    @Test
    public void testNoZeroLengthDireciton() {
        Ray ray = new Ray();
        Assertions.assertThrows(IllegalArgumentException.class,()-> ray.setDirection(new Vector3d(0, 0, 0)) );
    }
}
