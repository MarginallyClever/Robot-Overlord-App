package com.marginallyclever.convenience.helpers;

/**
 * {@link RayAABBHit} represents the result of a ray intersecting with an Axis-Aligned Bounding Box (AABB).
 */
public record RayAABBHit(boolean isHit, boolean isInside,
                         double tEnter, double tExit) {

    public RayAABBHit(boolean isHit, boolean isInside, double tEnter, double tExit) {
        this.isHit = isHit;
        this.isInside = isInside;
        this.tEnter = tEnter;
        this.tExit = tExit;
    }

    public static RayAABBHit miss() { return new RayAABBHit(false,false,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY); }

    public static RayAABBHit hit(boolean inside,double tEnter,double tExit) { return new RayAABBHit(true,inside,tEnter,tExit); }
}
