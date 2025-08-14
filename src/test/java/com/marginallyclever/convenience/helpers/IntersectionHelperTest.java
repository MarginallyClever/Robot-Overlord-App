package com.marginallyclever.convenience.helpers;

import com.marginallyclever.convenience.Cylinder;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.mesh.AABB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class IntersectionHelperTest {
    @Test
    public void testCylinderCylinder() {
        Cylinder c1 = new Cylinder();
        c1.SetP1(new Vector3d(0, 0, 0));
        c1.SetP2(new Vector3d(0, 0, 1));
        Cylinder c2 = new Cylinder();
        c2.SetP1(new Vector3d(0, 0, 4));
        c2.SetP2(new Vector3d(0, 0, 5));
        Cylinder c3 = new Cylinder();
        c3.SetP1(new Vector3d(0, 0, 0.5));
        c3.SetP2(new Vector3d(0, 0, 1.5));

        assertFalse(IntersectionHelper.cylinderCylinder(c1, c2));
        assertTrue(IntersectionHelper.cylinderCylinder(c1, c3));
    }

    @Test
    public void testCuboidCuboid() {
        AABB a = new AABB();
        a.setBounds(new Point3d(0, 0, 0), new Point3d(1, 1, 1));

        AABB b = new AABB();
        b.setBounds(new Point3d(2, 2, 2), new Point3d(3, 3, 3));

        AABB c = new AABB();
        c.setBounds(new Point3d(0.5, 0.5, 0.5), new Point3d(1.5, 1.5, 1.5));

        assertFalse(IntersectionHelper.cuboidCuboid(a, b));
        assertTrue(IntersectionHelper.cuboidCuboid(a, c));
    }

    @Test
    public void testRayBox() {
        AABB a = new AABB();
        a.setBounds(new Point3d(0, 0, 0), new Point3d(1, 1, 1));

        var p1 = new Point3d(0.5, 0.5, 1.5);
        var v2 = new Vector3d(0.0, 0.0, 1.0);
        var v3 = new Vector3d(0.0, 0.0, -1.0);
        var p4 = new Point3d(0.5, 0.5, 0.5);

        assertFalse(IntersectionHelper.rayBox(new Ray(p1, v2), a.getBoundsTop(), a.getBoundsBottom()).isHit());
        assertTrue(IntersectionHelper.rayBox(new Ray(p1, v3), a.getBoundsTop(), a.getBoundsBottom()).isHit());
        assertTrue(IntersectionHelper.rayBox(new Ray(p4, v3), a.getBoundsTop(), a.getBoundsBottom()).isHit());
    }

    @Test
    public void testcenterOfCircumscribedSphere() {
        var result = IntersectionHelper.centerOfCircumscribedSphere(
                new Vector3d(0, 0, 1),
                new Vector3d(1, 0, 0),
                new Vector3d(0, 1, 0),
                1);
        assertEquals(0, result.x, 1e-6);
        assertEquals(0, result.y, 1e-6);
        assertEquals(0, result.z, 1e-6);
    }

    @Test
    public void testRayTriangle() {
        Ray a = new Ray(new Point3d(0, 0, 0), new Vector3d(0, 0, 1));
        Point3d v0 = new Point3d(-1, -1, 1);
        Point3d v1 = new Point3d(1, 0, 1);
        Point3d v2 = new Point3d(0, 1, 1);
        var result = IntersectionHelper.rayTriangle(a, v0, v1, v2);
        assertEquals(1, result, 1e-6);
        Ray b = new Ray(new Point3d(0, 0, 0), new Vector3d(0, 0, -1));
        result = IntersectionHelper.rayTriangle(b, v0, v1, v2);
        assertEquals(Double.MAX_VALUE, result, 1e-6);
    }

    @Test
    public void sphereBox() {
        var p = new Point3d(0, 0,0);
        double r = 2;
        var min = new Point3d(1, 1,1);
        var max = new Point3d(3, 3,3);
        assert(IntersectionHelper.sphereBox(p,r*r,max,min));
        r=0.5;
        assert(!IntersectionHelper.sphereBox(p,r*r,max,min));
        p.set(4,4,4);
        assert(!IntersectionHelper.sphereBox(p,r*r,max,min));
        r=2;
        assert(IntersectionHelper.sphereBox(p,r*r,max,min));
        r=0.01;
        p.set(2,2,2);
        assert(IntersectionHelper.sphereBox(p,r*r,max,min));
        r=1;
        assert(IntersectionHelper.sphereBox(p,r*r,max,min));
        r=5;
        assert(IntersectionHelper.sphereBox(p,r*r,max,min));
    }
}