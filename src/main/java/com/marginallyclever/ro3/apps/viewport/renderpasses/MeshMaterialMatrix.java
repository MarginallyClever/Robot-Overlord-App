package com.marginallyclever.ro3.apps.viewport.renderpasses;

import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.vecmath.Matrix4d;

/**
 * A mesh and the material to render it with.
 * @param meshInstance The mesh to render.
 * @param material The material with which to render the mesh.
 * @param matrix The transformation matrix to apply to the mesh.
 */
public record MeshMaterialMatrix(MeshInstance meshInstance, Material material, Matrix4d matrix) {}
