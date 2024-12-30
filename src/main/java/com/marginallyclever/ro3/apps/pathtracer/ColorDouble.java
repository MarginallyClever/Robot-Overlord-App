package com.marginallyclever.ro3.apps.pathtracer;

import java.awt.*;

public class ColorDouble {
    public double r, g, b, a;

    public ColorDouble(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorDouble(double r, double g, double b) {
        this(r, g, b, 1.0);
    }

    public ColorDouble(Color c) {
        this(c.getRed() / 255.0,
            c.getGreen() / 255.0,
            c.getBlue() / 255.0,
            c.getAlpha() / 255.0);
    }

    public ColorDouble(ColorDouble other) {
        this(other.r, other.g, other.b, other.a);
    }

    public Color getColor() {
        return new Color(
                (int) (Math.max(0, Math.min(255, r * 255.0))),
                (int) (Math.max(0, Math.min(255, g * 255.0))),
                (int) (Math.max(0, Math.min(255, b * 255.0))),
                (int) (Math.max(0, Math.min(255, a * 255.0)))
        );
    }

    public void scale(double s) {
        r *= s;
        g *= s;
        b *= s;
        a *= s;
    }

    public void add(ColorDouble other) {
        r += other.r;
        g += other.g;
        b += other.b;
        a += other.a;
    }

    public void scale(ColorDouble other) {
        r *= other.r;
        g *= other.g;
        b *= other.b;
        a *= other.a;
    }

    public void set(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void set(ColorDouble other) {
        set(other.r, other.g, other.b, other.a);
    }
}
