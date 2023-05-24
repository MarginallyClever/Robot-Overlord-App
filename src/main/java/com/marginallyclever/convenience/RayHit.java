package com.marginallyclever.convenience;

import com.marginallyclever.robotoverlord.components.ShapeComponent;

import javax.vecmath.Vector3d;

/**
 * A ray hit is a record of a ray hitting a {@link ShapeComponent} at a certain distance.
 * @author Dan Royer
 * @since 2.5.0
 */
public class RayHit {
    public ShapeComponent target;
    public double distance;
    public final Vector3d normal;

    public RayHit(ShapeComponent target, double distance, Vector3d normal) {
        this.target = target;
        this.distance = distance;
        this.normal = normal;
    }
}
