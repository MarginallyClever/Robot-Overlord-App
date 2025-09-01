package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.spatialaccelerationstructure.BoundingVolumeHeirarchy;
import com.marginallyclever.ro3.apps.pathtracer.spatialaccelerationstructure.SpatialAccelerationStructure;
import com.marginallyclever.ro3.raypicking.Hit;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of {@link PathTriangle}s contained in an {@link SpatialAccelerationStructure} for optimized path tracing.
 */
public class PathMesh {
    private final List<PathTriangle> triangles = new LinkedList<>();
    private SpatialAccelerationStructure SAS = null;

    public void addTriangle(PathTriangle pt) {
        triangles.add(pt);
    }

    /**
     * Build the {@link SpatialAccelerationStructure} that will optimize the intersection math.
     */
    public void buildSAS() {
        SAS = new BoundingVolumeHeirarchy();
        for(PathTriangle triangle : triangles) {
            SAS.insert(triangle);
        }
        SAS.finishInserts();
    }

    /**
     * Intersect a ray with this mesh.
     * @param ray The ray to intersect with, in local space.
     * @return The RayHit object containing the intersection point and normal, or null if no intersection.
     */
    public Hit intersect(Ray ray) {
        PathTriangle bestTriangle = SAS.intersect(ray);
        if( bestTriangle == null ) return null;  // no hit

        double nearest = bestTriangle.intersectRay(ray);
        Vector3d normal = bestTriangle.normal;
        Point3d p = new Point3d();
        p.scaleAdd(nearest, ray.getDirection(), ray.getOrigin());
        return new Hit(null,nearest,normal,p, bestTriangle);
    }

    // if one side of the box is zero (a flat triangle) then add a tiny offset.
    private void addOffsetForZeroSize(Point3d boundTop, Point3d boundBottom) {
        final double OFFSET = 0.001; // a small offset to prevent zero-size bounds
        if(boundTop.x == boundBottom.x) {
            boundTop.x += OFFSET;
            boundBottom.x -= OFFSET;
        }
        if(boundTop.y == boundBottom.y) {
            boundTop.y += OFFSET;
            boundBottom.y -= OFFSET;
        }
        if(boundTop.z == boundBottom.z) {
            boundTop.z += OFFSET;
            boundBottom.z -= OFFSET;
        }
    }

    private void upperLimit(Point3d boundTop,Point3d p) {
        boundTop.x = Math.max(p.x, boundTop.x);
        boundTop.y = Math.max(p.y, boundTop.y);
        boundTop.z = Math.max(p.z, boundTop.z);
    }

    private void lowerLimit(Point3d boundBottom,Point3d p) {
        boundBottom.x = Math.min(p.x, boundBottom.x);
        boundBottom.y = Math.min(p.y, boundBottom.y);
        boundBottom.z = Math.min(p.z, boundBottom.z);
    }

    public PathTriangle getTriangle(int index) {
        return triangles.get(index);
    }

    public int getTriangleCount() {
        return triangles.size();
    }

    public PathTriangle getRandomTriangle() {
        if (triangles.isEmpty()) return null;
        int index = (int) (Math.random() * triangles.size());
        return triangles.get(index);
    }
}
