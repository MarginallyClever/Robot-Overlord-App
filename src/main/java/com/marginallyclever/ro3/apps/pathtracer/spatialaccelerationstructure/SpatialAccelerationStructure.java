package com.marginallyclever.ro3.apps.pathtracer.spatialaccelerationstructure;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.apps.pathtracer.PathTriangle;

/**
 * Interface for a Spatial Acceleration Structure (SAS) used in ray tracing to optimize intersection tests.
 */
public interface SpatialAccelerationStructure {
    /**
     * Add a new triangle to the SAS
     * @param triangle the triangle to add.
     * @return true if it was added.
     */
    boolean insert(PathTriangle triangle);

    /**
     * Called after all triangles are inserted.
     * Good for final step optimizing.
     */
    void finishInserts();

    /**
     * Finds the first intersecting triangle for a given ray within the spatial acceleration structure.
     *
     * @param ray the ray to test for intersection with the triangles in the structure
     * @return the first intersecting {@code PathTriangle}, or {@code null} if no intersection is found
     */
    PathTriangle intersect(Ray ray);
}
