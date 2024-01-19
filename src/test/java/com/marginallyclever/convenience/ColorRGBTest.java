package com.marginallyclever.convenience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Objects;

public class ColorRGBTest {
    @Test
    public void constructor() {
        ColorRGB c = new ColorRGB(0x7a, 0x7b, 0x7c);
        assert (c.getRed() == 0x7a);
        assert (c.getGreen() == 0x7b);
        assert (c.getBlue() == 0x7c);

        c = new ColorRGB(0x0a0b0c);
        assert (c.getRed() == 0x0a);
        assert (c.getGreen() == 0x0b);
        assert (c.getBlue() == 0x0c);
        assert (c.toInt() == 0x0a0b0c);

        c = new ColorRGB(Color.ORANGE);
        assert (c.getRed() == 255);
        assert (c.getGreen() == 200);
        assert (c.getBlue() == 0);
    }

    @Test
    public void setAndGet() {
        ColorRGB c = new ColorRGB();
        c.set(1,2,3);
        assert(Objects.equals(c.toColor(), new Color(1,2,3)));

        var b = new ColorRGB();
        b.set(c);
        assert(Objects.equals(b.toColor(), new Color(1,2,3)));
    }

    @Test
    public void parse() {
        var a = new ColorRGB(0x7a,0x7b,0x7c);
        assert(a.toString().equals("#7a7b7c"));

        ColorRGB c = new ColorRGB();
        c.parse(a.toString());
        assert (c.getRed() == 0x7a);
        assert (c.getGreen() == 0x7b);
        assert (c.getBlue() == 0x7c);

        c.parse("#6a6b6c");
        assert (c.getRed() == 0x6a);
        assert (c.getGreen() == 0x6b);
        assert (c.getBlue() == 0x6c);
    }

    @Test
    public void math() {
        var a = new ColorRGB(1,2,3);
        var b = new ColorRGB(4,5,6);
        var c = new ColorRGB(9,8,7);
        c.sub(b);
        assert(c.getRed()==5);
        assert(c.getGreen()==3);
        assert(c.getBlue()==1);
        c.add(b);
        assert(c.getRed()==9);
        assert(c.getGreen()==8);
        assert(c.getBlue()==7);

        Assertions.assertEquals(a.diff(c),Math.sqrt(8*8 + 6*6 + 4*4),1e-6);

        b.set(1,2,3);
        b.mul(3);
        assert(b.getRed()==3);
        assert(b.getGreen()==6);
        assert(b.getBlue()==9);
    }
}
