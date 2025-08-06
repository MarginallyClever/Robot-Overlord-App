package com.marginallyclever.ro3.apps.pathtracer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

public class ColorDoubleTest {
    private static final double EPSILON = 1e-6;
    @Test
    public void testColorDouble() {
        ColorDouble c = new ColorDouble(0.1, 0.2, 0.3);
        Assertions.assertEquals(0.1, c.r, EPSILON);
        Assertions.assertEquals(0.2, c.g, EPSILON);
        Assertions.assertEquals(0.3, c.b, EPSILON);
    }

    @Test
    public void testAdd() {
        ColorDouble a = new ColorDouble(0.1, 0.2, 0.3);
        ColorDouble b = new ColorDouble(0.4, 0.5, 0.6);
        ColorDouble c = new ColorDouble(a);
        c.add(b);
        Assertions.assertEquals(0.5, c.r, EPSILON);
        Assertions.assertEquals(0.7, c.g, EPSILON);
        Assertions.assertEquals(0.9, c.b, EPSILON);
    }

    @Test
    public void testMultiply() {
        ColorDouble a = new ColorDouble(0.1, 0.2, 0.3);
        ColorDouble b = new ColorDouble(0.4, 0.5, 0.6);
        ColorDouble c = new ColorDouble(a);
        c.multiply(b); // c = a*b
        Assertions.assertEquals(0.04, c.r, EPSILON);
        Assertions.assertEquals(0.1, c.g, EPSILON);
        Assertions.assertEquals(0.18, c.b, EPSILON);
    }

    @Test
    public void testGetColor() {
        ColorDouble c = new ColorDouble(0.1, 0.2, 0.3);
        Color color = c.getColor();
        Assertions.assertEquals(25, color.getRed());
        Assertions.assertEquals(51, color.getGreen());
        Assertions.assertEquals(76, color.getBlue());
    }
}
