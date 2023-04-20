package com.marginallyclever.robotoverlord.components.path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PathWalkerTest {
    @Test
    public void testPathWalker() {
        GCodePath path = new GCodePath();
        GCodePathElement element = new GCodePathElement("G1");
        element.setX(0);
        element.setY(0);
        path.addElement(element);
        element = new GCodePathElement("G1");
        element.setX(10);
        element.setY(15);
        path.addElement(element);
        element = new GCodePathElement("G1");
        element.setX(20);
        element.setY(25);
        path.addElement(element);

        double maxStepSize = 1;
        PathWalker walker = new PathWalker(path, maxStepSize);

        // Test the first point
        walker.next();
        Assertions.assertEquals(0, walker.getCurrentX(), 1e-6);
        Assertions.assertEquals(0, walker.getCurrentY(), 1e-6);

        // Test the second point
        walker.next();
        Assertions.assertEquals(10, walker.getCurrentX(), 1e-6);
        Assertions.assertEquals(15, walker.getCurrentY(), 1e-6);

        // Test the third point
        walker.next();
        Assertions.assertEquals(20, walker.getCurrentX(), 1e-6);
        Assertions.assertEquals(25, walker.getCurrentY(), 1e-6);
    }
}
