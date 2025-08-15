package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.ro3.mesh.AABB;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Triangle optimized for path tracing
 */
public class PathTriangle {
    public final Point3d a,b,c;
    public final Vector3d normal;
    private final Vector3d edge1, edge2;
    public final AABB bounds = new AABB();

    public PathTriangle(Point3d a, Point3d b, Point3d c, Vector3d normal) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.normal = normal;
        edge1 = new Vector3d(b.x - a.x, b.y - a.y, b.z - a.z);
        edge2 = new Vector3d(c.x - a.x, c.y - a.y, c.z - a.z);
        bounds.setBounds(a,a);
        bounds.grow(b);
        bounds.grow(c);
    }

    public AABB getBounds() {
        return bounds;
    }

    /**
     * Test if a ray intersects this triangle.
     * @param ray the ray to test
     * @return the distance along the ray to the intersection point, or Double.MAX_VALUE if there is no intersection.
     */
    public double intersectRay(Ray ray) {
        Vector3d pVec = new Vector3d();
        final Vector3d dir = ray.getDirection();
        pVec.cross(dir, edge2);
        double det = edge1.dot(pVec);
        if (det > -IntersectionHelper.EPSILON && det < IntersectionHelper.EPSILON) {
            return Double.MAX_VALUE; // Ray and triangle are parallel
        }

        double invDet = 1.0 / det;
        Vector3d tVec = new Vector3d();
        tVec.sub(ray.getOrigin(), a);
        double u = tVec.dot(pVec) * invDet;
        if (u < 0.0 || u > 1.0) return Double.MAX_VALUE;

        Vector3d qVec = new Vector3d();
        qVec.cross(tVec, edge1);
        double v = dir.dot(qVec) * invDet;
        if (v < 0.0 || u + v > 1.0) return Double.MAX_VALUE;

        double t = edge2.dot(qVec) * invDet;
        if (t < IntersectionHelper.EPSILON) return Double.MAX_VALUE; // Intersection is behind the ray origin

        return t;
    }

    public Point3d getRandomPointInside() {
        double r1 = Math.random();
        double r2 = Math.random();
        double sqrtR1 = Math.sqrt(r1);
        double u = 1 - sqrtR1;
        double v = r2 * sqrtR1;
        double w = 1 - u - v;

        return new Point3d(
                u * a.x + v * b.x + w * c.x,
                u * a.y + v * b.y + w * c.y,
                u * a.z + v * b.z + w * c.z
        );
    }

    public double getArea() {
        Vector3d cross = new Vector3d();
        cross.cross(edge1, edge2);
        return 0.5 * cross.length();
    }
}
