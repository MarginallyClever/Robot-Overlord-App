package com.marginallyclever.ro3.raypicking;

import com.marginallyclever.ro3.apps.pathtracer.PathTriangle;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A ray hit is a record of a ray hitting a {@link MeshInstance} at a certain distance.
 * @param target the MeshInstance that the {@link com.marginallyclever.convenience.Ray} intersected.
 * @param distance the distance from the {@link com.marginallyclever.convenience.Ray} origin to the point of contact.
 * @param normal the normal of the {@link com.marginallyclever.ro3.mesh.Mesh} at the point of contact, in world space.
 * @param point the point of contact in world space.
 * @param triangle the triangle at the point of contact.
 */
public record RayHit(MeshInstance target, double distance, Vector3d normal, Point3d point, PathTriangle triangle) {}
