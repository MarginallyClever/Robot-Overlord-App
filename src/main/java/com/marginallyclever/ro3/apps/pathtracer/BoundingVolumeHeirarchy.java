package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.mesh.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * <p>A {@link BoundingVolumeHeirarchy} uses {@link AABB}s and Surface Area Heuristics (SAH)
 * to organize {@link PathTriangle}s for fast intersection testing by classes like the {@link PathTracer}.</p>
 */
public class BoundingVolumeHeirarchy implements SpatialAccelerationStructure {
    private static final Logger logger = LoggerFactory.getLogger(BoundingVolumeHeirarchy.class);

    public static final int MAX_TRIANGLES_PER_LEAF = 4;
    // used in the cost function to determine where to split each node.
    public static final double C_TRAV = 1.0;  // the cost to travel between nodes
    public static final double C_INT = 10.0;  // the cost to intersect a triangle

    private final List<PathTriangle> triangles = new ArrayList<>();
    private final ForkJoinPool pool = ForkJoinPool.commonPool();

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
        root = pool.invoke(new BuildTask(triangles));
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

    private PathTriangle intersectNode(BVHNode node, Ray ray, double closest) {
        if(node==null) return null;

        var hit = node.box.intersect(ray);
        if (hit.tEnter() > closest) return null;

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
            return nearest;
        }

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
        return nearest;
    }
}
