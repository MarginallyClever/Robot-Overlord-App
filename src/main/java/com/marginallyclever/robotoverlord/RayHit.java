package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.ShapeComponent;

import javax.vecmath.Vector3d;

/**
 * A ray hit is a record of a ray hitting a {@link ShapeComponent} at a certain distance.
 */
@Deprecated
public record RayHit(ShapeComponent target, double distance, Vector3d normal) {}
