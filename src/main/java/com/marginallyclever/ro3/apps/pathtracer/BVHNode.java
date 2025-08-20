package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.ro3.mesh.AABB;

import java.util.List;

/**
 * A node in the {@link BoundingVolumeHeirarchy}.
 */
public class BVHNode {
    public AABB box;
    public BVHNode left, right;
    public List<PathTriangle> tris; // only non-null if leaf
}
