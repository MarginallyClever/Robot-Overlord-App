package com.marginallyclever.ro3.apps.pathtracer;

/**
 * Simple 2D integer rectangle class.
 */
public class Rectangle2i {
    public Point2i max,min;

    public Rectangle2i(Point2i min, Point2i max) {
        this.min = min;
        this.max = max;
    }
}
