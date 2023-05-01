package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import com.marginallyclever.robotoverlord.systems.render.gcodepath.GCodePath;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.GCodePathElement;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.PathWalker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point3d;

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
        element.setZ(25);
        path.addElement(element);
        element = new GCodePathElement("G1");
        element.setX(20);
        element.setY(25);
        element.setZ(0);
        path.addElement(element);

        double maxStepSize = 1;
        PathWalker walker = new PathWalker(null,path, maxStepSize);

        // Test the first point
        walker.next();
        Point3d a = walker.getCurrentPosition();
        Assertions.assertEquals(0, a.x, 1e-6);
        Assertions.assertEquals(0, a.y, 1e-6);
        Assertions.assertEquals(0, a.z, 1e-6);

        // Test the second point
        walker.next();
        a = walker.getCurrentPosition();
        Assertions.assertEquals(10, a.x, 1e-6);
        Assertions.assertEquals(15, a.y, 1e-6);
        Assertions.assertEquals(25, a.z, 1e-6);

        // Test the third point
        walker.next();
        a = walker.getCurrentPosition();
        Assertions.assertEquals(20, a.x, 1e-6);
        Assertions.assertEquals(25, a.y, 1e-6);
        Assertions.assertEquals(0, a.z, 1e-6);
    }
}
