package com.marginallyclever.ro3.raypicking;

import com.marginallyclever.ro3.node.nodes.MeshInstance;

import javax.vecmath.Vector3d;

/**
 * A ray hit is a record of a ray hitting a {@link MeshInstance} at a certain distance.
 * @author Dan Royer
 * @since 2.5.0
 */
public record RayHit(MeshInstance target, double distance, Vector3d normal) {}
