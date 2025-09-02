package com.marginallyclever.ro3.apps.pathtracer.spatialaccelerationstructure;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.PathTriangle;
import com.marginallyclever.ro3.mesh.AABB;

import java.util.LinkedList;
import java.util.List;

/**
 * OctreeNode is a node in an octree structure used for spatial partitioning.
 */
public class OctreeNode implements SpatialAccelerationStructure {
    private static final int MAX_DEPTH = 10;

    private final AABB bounds;
    private final List<PathTriangle> triangles = new LinkedList<>();
    private OctreeNode[] children = null;

    public OctreeNode(AABB bounds) {
        super();
        this.bounds = bounds;
    }

    @Override
    public boolean insert(PathTriangle triangle) {
        return insert(triangle,0);
    }

    private boolean insert(PathTriangle triangle, int depth) {
        if (!contains(triangle)) return false;

        if (depth < MAX_DEPTH) {
            if (children == null) subdivide();
            for (OctreeNode child : children) {
                if(child.insert(triangle, depth + 1)) return true;
            }
        }

        triangles.add(triangle);
        return true;
    }

    @Override
    public void finishInserts() {
        trim();
    }

    /**
     * Remove unused children
     */
    private void trim() {
        // if all children are empty, remove them.
        if (children == null) return;

        boolean allEmpty = true;
        for (OctreeNode child : children) {
            child.trim();
            if (!child.triangles.isEmpty() || child.children != null) {
                allEmpty = false;
            }
        }
        if (allEmpty) {
            children = null; // remove children if they are all empty
        }
    }

    private boolean contains(PathTriangle t) {
        var boxTop = bounds.getBoundsTop();
        var boxBot = bounds.getBoundsBottom();
        // use triangle center and radius to check if it fits in the AABB
        var tBounds = t.getBounds();
        var triTop = tBounds.getBoundsTop();
        var triBot = tBounds.getBoundsBottom();

        return (boxTop.x >= triTop.x && boxTop.y >= triTop.y && boxTop.z >= triTop.z) &&
               (boxBot.x <= triBot.x && boxBot.y <= triBot.y && boxBot.z <= triBot.z);
    }

    private void subdivide() {
        AABB[] octants = bounds.subdivide();
        children = new OctreeNode[8];
        for (int i = 0; i < 8; i++) {
            children[i] = new OctreeNode(octants[i]);
        }
    }

    @Override
    public PathTriangle intersect(Ray ray) {
        var test = bounds.intersect(ray);
        if (!test.isHit()) return null;

        PathTriangle closestHit = null;
        double closestDist = ray.getMaxDistance();

        for (PathTriangle triangle : triangles) {
            if(!triangle.bounds.intersect(ray).isHit()) continue;
            double dist = triangle.intersectRay(ray);
            if (dist < closestDist) {
                closestDist = dist;
                closestHit = triangle;
            }
        }

        if (children != null) {
            for (OctreeNode child : children) {
                PathTriangle triangle = child.intersect(ray);
                if (triangle == null) continue;
                double dist = triangle.intersectRay(ray);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestHit = triangle;
                }
            }
        }

        return closestHit;
    }

    public void print() {
        print(0);
    }

    private void print(int depth) {
        System.out.println("  ".repeat(depth) +
                "OctreeNode at depth " + depth +
                " with bounds " + bounds +
                " containing " + triangles.size() + " triangles.");
        if (children != null) {
            for (OctreeNode child : children) {
                child.print(depth + 1);
            }
        }
    }
}
