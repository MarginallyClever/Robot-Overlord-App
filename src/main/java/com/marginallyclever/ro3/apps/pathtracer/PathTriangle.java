package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.ro3.mesh.AABB;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Triangle optimized for path tracing
 */
public class PathTriangle {
    public final PathPoint a,b,c;
    private final Vector3d edge1, edge2;
    public final AABB bounds = new AABB();
    // for finding texture UVs
    private final double d00, d01, d11, denom;
    private final double area;
    private final double [] centroid = new double[3];

    public PathTriangle(PathPoint a, PathPoint b, PathPoint c) {
        this.a = a;
        this.b = b;
        this.c = c;
        edge1 = new Vector3d(b.p.x - a.p.x, b.p.y - a.p.y, b.p.z - a.p.z);
        edge2 = new Vector3d(c.p.x - a.p.x, c.p.y - a.p.y, c.p.z - a.p.z);
        bounds.setBounds(a.p,a.p);
        bounds.grow(b.p);
        bounds.grow(c.p);

        d00 = edge1.dot(edge1);
        d01 = edge1.dot(edge2);
        d11 = edge2.dot(edge2);
        denom = d00 * d11 - d01 * d01;

        Point3d middle = new Point3d(bounds.getBoundsBottom());
        middle.add(bounds.getBoundsTop());
        middle.scale(0.5);
        this.centroid[0] = middle.x;
        this.centroid[1] = middle.y;
        this.centroid[2] = middle.z;

        // area
        Vector3d cross = new Vector3d();
        cross.cross(edge1, edge2);
        area = 0.5 * cross.length();
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
        tVec.sub(ray.getOrigin(), a.p);
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

    /**
     * Get a random point inside the triangle using barycentric coordinates.
     * @return a random point inside the triangle.
     */
    public Point3d getRandomPointInside() {
        double r1 = Math.random();
        double r2 = Math.random();
        double sqrtR1 = Math.sqrt(r1);
        double u = 1.0 - sqrtR1;
        double v = r2 * sqrtR1;
        double w = 1.0 - u - v;

        return new Point3d(
                u * a.p.x + v * b.p.x + w * c.p.x,
                u * a.p.y + v * b.p.y + w * c.p.y,
                u * a.p.z + v * b.p.z + w * c.p.z
        );
    }

    public double getArea() {
        return area;
    }

    /**
     * Get the UV coordinates of the texture at a specific point on the triangle.
     * @param point the 3d world point of intersection with the triangle.
     * @return the UV coordinates of the texture at the point.
     */
    public Point2d getUVAt(Point3d point) {
        Vector3d v2 = new Vector3d();
        v2.sub(point, a.p);

        double d20 = v2.dot(edge1);
        double d21 = v2.dot(edge2);

        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        return new Point2d(
                u * a.t.x + v * b.t.x + w * c.t.x,
                u * a.t.y + v * b.t.y + w * c.t.y
        );
    }

    public Color getColorAt(Point3d point) {
        Vector3d v2 = new Vector3d();
        v2.sub(point, a.p);

        double d20 = v2.dot(edge1);
        double d21 = v2.dot(edge2);

        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        int red   = (int)(u * a.c.getRed() + v * b.c.getRed() + w * c.c.getRed());
        int green = (int)(u * a.c.getGreen() + v * b.c.getGreen() + w * c.c.getGreen());
        int blue  = (int)(u * a.c.getBlue() + v * b.c.getBlue() + w * c.c.getBlue());
        int alpha = (int)(u * a.c.getAlpha() + v * b.c.getAlpha() + w * c.c.getAlpha());

        return new Color(red, green, blue, alpha);
    }

    /**
     * Get the centroid of the triangle along a specific axis.
     * @param axis the axis to get the centroid along (0 for x, 1 for y, or 2 for z)
     * @return the centroid coordinate along the specified axis
     */
    public double getCentroidAxis(int axis) {
        return centroid[axis];
    }

    public Vector3d getNormalAt(Point3d inside) {
        // interpolate the normals using barycentric coordinates
        Vector3d v2 = new Vector3d();
        v2.sub(inside, a.p);

        double d20 = v2.dot(edge1);
        double d21 = v2.dot(edge2);

        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;

        Vector3d normal = new Vector3d(
                u * a.n.x + v * b.n.x + w * c.n.x,
                u * a.n.y + v * b.n.y + w * c.n.y,
                u * a.n.z + v * b.n.z + w * c.n.z
        );
        normal.normalize();
        return normal;
    }
}
