package com.marginallyclever.ro3.apps.pathtracer;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * A point in a PathTriangle.
 */
public class PathPoint {
    public Point3d p;
    public Vector3d n;
    public Color c;
    public Point2d t;

    public PathPoint(Point3d worldPoint, Vector3d normal, Color color, Point2d textureCoordinate) {
        p = worldPoint;
        n = normal;
        c = color;
        t = textureCoordinate;
    }
}
