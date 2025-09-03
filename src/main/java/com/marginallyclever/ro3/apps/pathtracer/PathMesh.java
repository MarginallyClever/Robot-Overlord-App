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

    public int getTriangleCount() {
        return triangles.size();
    }

    public PathTriangle getRandomTriangle() {
        if (triangles.isEmpty()) return null;
        int index = (int) (Math.random() * triangles.size());
        return triangles.get(index);
    }
}
