package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.mesh.AABB;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

/**
 * OctreeNode is a node in an octree structure used for spatial partitioning.
 */
public class OctreeNode {
    private static final int MAX_TRIANGLES = 10;
    private static final int MAX_DEPTH = 10;

    private final AABB bounds;
    private final List<PathTriangle> triangles = new ArrayList<>();
    private OctreeNode[] children = null;

    public OctreeNode(AABB bounds) {
        this.bounds = bounds;
    }

    public void insert(PathTriangle triangle, int depth) {
        if (!contains(triangle)) return;

        if (triangles.size() < MAX_TRIANGLES || depth >= MAX_DEPTH) {
            triangles.add(triangle);
        } else {
            if (children == null) subdivide();
            for (OctreeNode child : children) {
                child.insert(triangle, depth + 1);
            }
        }
    }

    private boolean contains(PathTriangle t) {
        var top = bounds.getBoundsTop();
        var bot = bounds.getBoundsBottom();
        // use triangle center and radius to check if it fits in the AABB
        double radius = t.radius;
        Point3d center = t.center;
        return center.x + radius >= bot.x && center.x - radius <= top.x &&
               center.y + radius >= bot.y && center.y - radius <= top.y &&
               center.z + radius >= bot.z && center.z - radius <= top.z;
    }

    private void subdivide() {
        AABB[] octants = bounds.subdivide();
        children = new OctreeNode[8];
        for (int i = 0; i < 8; i++) {
            children[i] = new OctreeNode(octants[i]);
        }
    }

    public PathTriangle intersect(Ray ray) {
        if (!bounds.intersect(ray)) return null;

        PathTriangle closestHit = null;
        double closestDist = ray.maxDistance();

        for (PathTriangle triangle : triangles) {
            double dist = triangle.intersectRay(ray);
            if(dist < closestDist) {
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
}
