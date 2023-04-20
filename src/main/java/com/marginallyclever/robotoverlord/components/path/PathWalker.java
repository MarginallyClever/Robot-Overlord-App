package com.marginallyclever.robotoverlord.components.path;
import java.util.Iterator;

/**
 * Walks a {@link GCodePath}, breaking arcs into line segments.
 * @author Dan Royer
 */
public class PathWalker {
    private final Iterator<GCodePathElement> iterator;
    private GCodePathElement currentElement;
    private final double maxStepSize;
    private double currentX, currentY, currentZ;
    private int currentArcSegment, totalArcSegments;
    boolean relativeMoves=false;
    double centerX, centerY, radius;

    /**
     * Initialize the path walker.
     * @param gCodePath the path to walk
     * @param maxStepSize the maximum distance between points in arc segments.
     */
    public PathWalker(GCodePath gCodePath, double maxStepSize) {
        this.iterator = gCodePath.getElements().iterator();
        this.maxStepSize = maxStepSize;
        this.currentX = 0;
        this.currentY = 0;
        this.currentZ = 0;
        this.currentArcSegment = 0;
        this.totalArcSegments = 0;
    }

    public boolean hasNext() {
        return iterator.hasNext() || currentArcSegment < totalArcSegments;
    }

    public void next() {
        if (currentArcSegment < totalArcSegments) {
            double angleFraction = (double) currentArcSegment / totalArcSegments;
            double angleDelta = currentElement.isClockwise() ? -angleFraction * 2 * Math.PI : angleFraction * 2 * Math.PI;

            currentX = centerX + radius * Math.cos(angleDelta);
            currentY = centerY + radius * Math.sin(angleDelta);
            currentArcSegment++;
            return;
        }

        currentElement = iterator.next();
        String command = currentElement.getCommand();

        if (command.equals("G90")) {
            relativeMoves = false;
            return;
        } else if (command.equals("G91")) {
            relativeMoves = true;
            return;
        }

        if (command.equals("G2") || command.equals("G3")) {
            double i = currentElement.getI();
            double j = currentElement.getJ();
            centerX = currentElement.getX() - i;
            centerY = currentElement.getY() - j;
            radius = Math.sqrt(i * i + j * j);
            double circumference = 2.0 * Math.PI * radius;
            totalArcSegments = (int)Math.ceil(circumference / maxStepSize);
            currentArcSegment = 1;
        } else {
            totalArcSegments = 0;
            currentArcSegment = 0;
        }

        if (relativeMoves) {
            currentX += currentElement.getX();
            currentY += currentElement.getY();
            currentZ += currentElement.getZ();
        } else {
            currentX = currentElement.getX();
            currentY = currentElement.getY();
            currentZ = currentElement.getZ();
        }
    }

    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public double getCurrentZ() {
        return currentZ;
    }

    public GCodePathElement getCurrentElement() {
        return currentElement;
    }
}

