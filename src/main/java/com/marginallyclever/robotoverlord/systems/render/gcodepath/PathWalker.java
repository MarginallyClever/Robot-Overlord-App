package com.marginallyclever.robotoverlord.systems.render.gcodepath;
import com.marginallyclever.robotoverlord.components.PoseComponent;

import javax.vecmath.Point3d;
import java.util.Iterator;

/**
 * Walks a {@link GCodePath}, breaking arcs into line segments.
 * @author Dan Royer
 */
public class PathWalker {
    private final Iterator<GCodePathElement> iterator;
    private GCodePathElement currentElement;
    private final double maxStepSize;
    private final Point3d currentPosition = new Point3d();
    private int currentArcSegment, totalArcSegments;
    boolean relativeMoves=false;
    double centerX, centerY, radius;
    /**
     * The {@link PoseComponent} which converts local gcodepath coordinates to world coordinates.
     */
    private final PoseComponent poseComponent;

    /**
     * Initialize the gcodepath walker.
     * @param poseComponent converts local gcodepath coordinates to world coordinates.
     * @param path the gcodepath to walk.
     * @param maxStepSize the maximum distance between points in arc segments.
     */
    public PathWalker(PoseComponent poseComponent, GCodePath path,double maxStepSize) {
        this.poseComponent = poseComponent;
        this.iterator = path.getElements().iterator();
        this.maxStepSize = maxStepSize;
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

            currentPosition.x = centerX + radius * Math.cos(angleDelta);
            currentPosition.y = centerY + radius * Math.sin(angleDelta);
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
            currentPosition.x += currentElement.getX();
            currentPosition.y += currentElement.getY();
            currentPosition.z += currentElement.getZ();
        } else {
            currentPosition.x = currentElement.getX();
            currentPosition.y = currentElement.getY();
            currentPosition.z = currentElement.getZ();
        }
    }

    public GCodePathElement getCurrentElement() {
        return currentElement;
    }


    public Point3d getCurrentPosition() {
        if(poseComponent==null) return new Point3d(currentPosition);

        Point3d result = new Point3d();
        poseComponent.getWorld().transform(currentPosition,result);
        return result;
    }
}

