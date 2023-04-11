package com.marginallyclever.convenience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Unit tests for the Plane class.
 * @author Dan Royer
 */
public class PlaneTest {
    @Test
    public void testPlane() {
        Plane p = new Plane();
        Assertions.assertEquals(0, p.distance);
        Assertions.assertEquals(0, p.normal.x);
        Assertions.assertEquals(0, p.normal.y);
        Assertions.assertEquals(1, p.normal.z);
    }

    /**
     * test intersection of plane and a {@link Ray}
     */
    @Test
    public void testIntersection() {
        Plane p = new Plane();
        Ray r = new Ray(new Point3d(0,0,-1),new Vector3d(0,0,1));
        Point3d intersection = new Point3d();
        Assertions.assertTrue(p.intersect(r, intersection));
        Assertions.assertEquals(0, intersection.x);
        Assertions.assertEquals(0, intersection.y);
        Assertions.assertEquals(0, intersection.z);
    }

    /**
     * test intersection of plane and a {@link Ray}
     */
    @Test
    public void testIntersection2() {
        Plane p = new Plane();
        Ray r = new Ray(new Point3d(0,0,1),new Vector3d(0,0,-1));
        Point3d intersection = new Point3d();
        Assertions.assertTrue(p.intersect(r, intersection));
        Assertions.assertEquals(0, intersection.x);
        Assertions.assertEquals(0, intersection.y);
        Assertions.assertEquals(0, intersection.z);
    }

    /**
     * test no intersection of plane and a {@link Ray}
     */
    @Test
    public void testIntersection3() {
        Plane p = new Plane();
        Ray r = new Ray(new Point3d(0,0,1),new Vector3d(0,0,1));
        Point3d intersection = new Point3d();
        Assertions.assertEquals(-1,p.intersectDistance(r));
        Assertions.assertTrue(p.intersect(r, intersection));
        Assertions.assertEquals(0, intersection.x);
        Assertions.assertEquals(0, intersection.y);
        Assertions.assertEquals(0, intersection.z);
    }
}
