package com.marginallyclever.convenience;

import java.awt.*;
import java.security.InvalidParameterException;

/**
 * RGB color class.  Values should be 0...255.
 */
public class ColorRGB {
    public int red;
    public int green;
    public int blue;

    public ColorRGB() {
        this(0,0,0);
    }

    public ColorRGB(int r, int g, int b) {
        red = r;
        green = g;
        blue = b;
    }

    public ColorRGB(ColorRGB x) {
        set(x);
    }

    public ColorRGB(int pixel) {
        set(pixel);
    }

    public void set(int hex) {
        red = ((hex >> 16) & 0xff);
        green = ((hex >> 8) & 0xff);
        blue = ((hex) & 0xff);
    }

    public ColorRGB(Color c) {
        red = c.getRed();
        green = c.getGreen();
        blue = c.getBlue();
    }

    public int toInt() {
        return ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
    }

    public void set(ColorRGB x) {
        red = x.red;
        green = x.green;
        blue = x.blue;
    }

    public ColorRGB set(int r, int g, int b) {
        red = r;
        green = g;
        blue = b;
        return this;
    }

    public ColorRGB sub(ColorRGB x) {
        red -= x.red;
        green -= x.green;
        blue -= x.blue;
        return this;
    }

    public ColorRGB add(ColorRGB x) {
        red += x.red;
        green += x.green;
        blue += x.blue;
        return this;
    }

    public ColorRGB mul(double f) {
        red = (int) (red * f);
        green = (int) (green * f);
        blue = (int) (blue * f);
        return this;
    }

    public float diff(ColorRGB o) {
        int rDiff = o.red - this.red;
        int gDiff = o.green - this.green;
        int bDiff = o.blue - this.blue;
        int distanceSquared = rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
        return (float) Math.sqrt(distanceSquared);
    }

    @Override
    public String toString() {
        return "#" + Integer.toString(toInt(),16);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void parse(String arg0) throws NumberFormatException, InvalidParameterException {
        if( arg0 == null ) throw new InvalidParameterException("arg0 is null");
        if( arg0.startsWith("#") ) arg0 = arg0.substring(1);
        if( arg0.startsWith("0x") ) arg0 = arg0.substring(2);
        int size = arg0.length();
        if( size > 8 || size < 6 ) {
            throw new InvalidParameterException("arg0 must be 6 or 8 characters long in hex format.");
        }
        set(Integer.parseInt(arg0, 16));
    }

    public Color toColor() {
        return new Color(red, green, blue);
    }
}
