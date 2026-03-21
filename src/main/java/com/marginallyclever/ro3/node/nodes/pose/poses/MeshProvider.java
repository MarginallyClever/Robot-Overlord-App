package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Matrix4d;

/**
 * A {@link MeshProvider} is a node that can supply a {@link Mesh} for rendering.
 * This interface captures the contract that {@link com.marginallyclever.ro3.apps.viewport.renderpass.DrawMeshes}
 * requires from any renderable mesh node.
 */
public interface MeshProvider {
    /**
     * @return the {@link Mesh} to be rendered, or null if none is set.
     */
    Mesh getMesh();

    /**
     * @return true if this node should be included in rendering.
     */
    boolean isActive();

    /**
     * @return true if this node should cast shadows.
     */
    boolean getHasShadow();

    /**
     * @return the world transform matrix for this node.
     */
    Matrix4d getWorld();
}
