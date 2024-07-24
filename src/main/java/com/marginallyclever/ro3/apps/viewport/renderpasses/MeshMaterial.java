package com.marginallyclever.ro3.apps.viewport.renderpasses;

import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

/**
 * A mesh and the material to render it with.
 * @param meshInstance The mesh to render.
 * @param material The material with which to render the mesh.
 */
public record MeshMaterial(MeshInstance meshInstance, Material material) {}
