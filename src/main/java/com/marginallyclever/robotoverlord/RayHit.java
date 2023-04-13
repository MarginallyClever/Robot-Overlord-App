package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.ShapeComponent;

/**
 * A ray hit is a record of a ray hitting a {@link ShapeComponent} at a certain distance.
 * @author Dan Royer
 * @since 2.5.0
 */
public class RayHit {
    public ShapeComponent target;
    public double distance;

    public RayHit(ShapeComponent target, double distance) {
        this.target = target;
        this.distance = distance;
    }
}
