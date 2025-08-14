package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.mesh.AABB;
import com.marginallyclever.ro3.raypicking.RayHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.LinkedList;
import java.util.List;

/**
 * A collection of {@link PathTriangle}s contained in an {@link OctreeNode} for optimized path tracing.
 */
public class PathMesh {
    private static final Logger logger = LoggerFactory.getLogger(PathMesh.class);

    private final List<PathTriangle> triangles = new LinkedList<>();
    private final AABB boundingBox = new AABB();
    private OctreeNode octreeRoot = null;

    public void addTriangle(PathTriangle pt) {
        triangles.add(pt);
    }

    public void buildOctree() {
        updateCuboid();
        octreeRoot = new OctreeNode(boundingBox);
        int misses=0;
        for(PathTriangle triangle : triangles) {
            if(!octreeRoot.insert(triangle,0)) misses++;
        }
        if(misses>0) logger.warn("Failed to insert {} triangles into octree.", misses);
        octreeRoot.trim();
        //octreeRoot.print();
    }

    /**
     * Intersect a ray with this mesh.
     * @param ray The ray to intersect with, in local space.
     * @return The RayHit object containing the intersection point and normal, or null if no intersection.
     */
    public RayHit intersect(Ray ray) {
        var test = boundingBox.intersect(ray);
        if(!test.isHit()) return null;  // no hit

        PathTriangle bestTriangle = octreeRoot.intersect(ray);
        if( bestTriangle == null ) return null;  // no hit

        double nearest = bestTriangle.intersectRay(ray);
        Vector3d normal = bestTriangle.normal;
        Point3d p = new Point3d();
        p.scaleAdd(nearest, ray.direction(), ray.origin());
        return new RayHit(null,nearest,normal,p, bestTriangle);
    }


    /**
     * Force recalculation of the minimum bounding box to contain all the triangles.
     * Done automatically every time updateBuffers() is called.
     * Meaningless if there is no vertexArray of points.
     */
    private void updateCuboid() {
        Point3d boundBottom = new Point3d(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);
        Point3d boundTop = new Point3d(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE);

        for(PathTriangle triangle : triangles) {
            upperLimit(boundTop,triangle.a);
            upperLimit(boundTop,triangle.b);
            upperLimit(boundTop,triangle.c);
            lowerLimit(boundBottom,triangle.a);
            lowerLimit(boundBottom,triangle.b);
            lowerLimit(boundBottom,triangle.c);
        }
        // if one side of the box is zero (a flat triangle) then add a tiny offset.
        addOffsetForZeroSize(boundTop, boundBottom);
        boundingBox.setBounds(boundTop, boundBottom);
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
