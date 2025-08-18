package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.mesh.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <p>A {@link BoundingVolumeHeirarchy} uses {@link AABB}s and Surface Area Heuristics (SAH)
 * to organize {@link PathTriangle}s for fast intersection testing by classes like the {@link PathTracer}.</p>
 */
public class BoundingVolumeHeirarchy implements SpatialAccelerationStructure {
    private static final Logger logger = LoggerFactory.getLogger(BoundingVolumeHeirarchy.class);

    private static final int MAX_TRIANGLES_PER_LEAF = 4;
    // used in the cost function to determine where to split each node.
    private static final double C_TRAV = 1.0;  // the cost to travel between nodes
    private static final double C_INT = 10.0;  // the cost to intersect a triangle

    private final List<PathTriangle> triangles = new ArrayList<>();

    private static class BVHNode {
        public AABB box;
        public BVHNode left, right;
        public List<PathTriangle> tris; // only non-null if leaf
    }
    private BVHNode root;

    public BoundingVolumeHeirarchy() {
        super();
    }

    /**
     * Add a new triangle to the SAS
     * @param triangle the triangle to add.
     * @return true if it was added.
     */
    @Override
    public boolean insert(PathTriangle triangle) {
        triangles.add(triangle);
        return true;
    }

    /**
     * Called after all triangles are inserted.
     * Good for final step optimizing.
     */
    @Override
    public void finishInserts() {
        logger.debug("build started");
        root = build(new ArrayList<>(triangles), 0);
        logger.debug("build finished");
        triangles.clear(); // free memory
    }

    /**
     * Finds the first intersecting triangle for a given ray within the spatial acceleration structure.
     *
     * @param ray the ray to test for intersection with the triangles in the structure
     * @return the first intersecting {@code PathTriangle}, or {@code null} if no intersection is found
     */
    @Override
    public PathTriangle intersect(Ray ray) {
        if (root == null) return null;
        try {
            return intersectNode(root, ray, Double.MAX_VALUE);
        } catch(Exception e) {
            logger.error("intersect", e);
            return null;
        }
    }

    private BVHNode build(List<PathTriangle> tris, int depth) {
        BVHNode node = new BVHNode();

        // Compute bounds of all triangles in this node
        AABB box = new AABB();
        for (PathTriangle t : tris) {
            box.grow(t.bounds.getBoundsTop());
            box.grow(t.bounds.getBoundsBottom());
        }
        node.box = box;

        if (tris.size() <= MAX_TRIANGLES_PER_LEAF) {
            node.tris = tris;
            return node;
        }

        //splitByLongestDimension(node,tris,box,depth);
        splitBySweep(node,tris,box,depth);
        //splitByBinning(node,tris,box,depth);  // not tested

        return node;
    }

    private void splitBySweep(BVHNode node, List<PathTriangle> tris, AABB box, int depth) {
        // Find best SAH split
        double bestCost = C_INT * tris.size();
        int bestAxis = -1;
        int bestIndex = -1;

        for (int axis = 0; axis < 3; axis++) {
            final var myAxis = axis;
            tris.sort(Comparator.comparingDouble(t -> t.bounds.getCentroidAxis(myAxis)));

            // Prefix and suffix bounds for sweep
            int n = tris.size();
            AABB[] leftBounds = new AABB[n];
            AABB[] rightBounds = new AABB[n];

            AABB acc = new AABB();
            for (int i = 0; i < n; i++) {
                acc = new AABB(acc);
                var bounds = tris.get(i).bounds;
                acc.grow(bounds.getBoundsBottom());
                acc.grow(bounds.getBoundsTop());
                leftBounds[i] = acc;
            }
            acc = new AABB();
            for (int i = n - 1; i >= 0; i--) {
                acc = new AABB(acc);
                var bounds = tris.get(i).bounds;
                acc.grow(bounds.getBoundsBottom());
                acc.grow(bounds.getBoundsTop());
                rightBounds[i] = acc;
            }

            for (int i = 1; i < n; i++) {
                int nLeft = i;
                int nRight = n - i;
                double sahCost = C_TRAV
                        + (leftBounds[i - 1].surfaceArea() / box.surfaceArea()) * nLeft * C_INT
                        + (rightBounds[i].surfaceArea() / box.surfaceArea()) * nRight * C_INT;

                if (sahCost < bestCost) {
                    bestCost = sahCost;
                    bestAxis = axis;
                    bestIndex = i;
                }
            }
        }

        if (bestAxis == -1) {
            // No good split found -> make leaf
            node.tris = tris;
            return;
        }

        // Split at best axis/index
        final var myAxis = bestAxis;
        tris.sort(Comparator.comparingDouble(t -> t.bounds.getCentroidAxis(myAxis)));
        List<PathTriangle> left = new ArrayList<>(tris.subList(0, bestIndex));
        List<PathTriangle> right = new ArrayList<>(tris.subList(bestIndex, tris.size()));

        node.left = build(left, depth + 1);
        node.right = build(right, depth + 1);
    }

    private void splitByLongestDimension(BVHNode node,List<PathTriangle> tris,AABB box,int depth) {
        // Choose split axis: longest dimension of the box
        var top = box.getBoundsTop();
        var bottom = box.getBoundsBottom();
        double dx = top.x - bottom.x;
        double dy = top.y - bottom.y;
        double dz = top.z - bottom.z;
        int axis = (dx > dy && dx > dz) ? 0 : (dy > dz ? 1 : 2);

        // Sort by centroid along that axis
        tris.sort( Comparator.comparingDouble(t -> t.bounds.getCentroidAxis(axis) ) );
        int mid = tris.size() / 2;

        node.left = build(tris.subList(0, mid), depth + 1);
        node.right = build(tris.subList(mid, tris.size()), depth + 1);
    }

    private PathTriangle intersectNode(BVHNode node, Ray ray, double closest) {
        if(node==null) return null;

        var hit = node.box.intersect(ray);
        if (hit.tEnter() > closest) {
            return null;
        }

        PathTriangle nearest = null;
        if (node.tris != null) {
            // Leaf: test each triangle
            for (PathTriangle t : node.tris) {
                double d = t.intersectRay(ray);
                if (d < closest) {
                    closest = d;
                    nearest = t;
                }
            }
        } else {
            PathTriangle hitL = intersectNode(node.left, ray, closest);
            if (hitL != null) {
                closest = hitL.intersectRay(ray);
                nearest = hitL;
            }
            PathTriangle hitR = intersectNode(node.right, ray, closest);
            if (hitR != null) {
                double d = hitR.intersectRay(ray);
                if (d < closest) {
                    nearest = hitR;
                }
            }
        }
        return nearest;
    }
}
