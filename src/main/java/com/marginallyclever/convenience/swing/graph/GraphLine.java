package com.marginallyclever.convenience.swing.graph;

import com.marginallyclever.convenience.ColorRGB;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A line in a {@link GraphModel}.
 * @author Dan Royer
 * @since 2.10.0
 */
public class GraphLine {
    private final List<Point2D> points = new ArrayList<>();
    private Color color = new Color(0);

    public void addPoint(double x,double y) {
        points.add(new Point2D.Double(x,y));
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }
}
