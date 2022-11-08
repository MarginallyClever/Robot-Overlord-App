package com.marginallyclever.convenience;

import java.awt.*;
import java.security.InvalidParameterException;

/**
 * RGB color class
 * @author Dan Royer
 *
 */
public class ColorRGB {
  public int red = 0;
  public int green = 0;
  public int blue = 0;

  public ColorRGB(int r, int g, int b) {
    red = r;
    green = g;
    blue = b;
  }

  public ColorRGB(ColorRGB x) {
    set(x);
  }

  public ColorRGB(int pixel) {
    int r = ((pixel >> 16) & 0xff);
    int g = ((pixel >> 8) & 0xff);
    int b = ((pixel) & 0xff);
    set(r, g, b);
  }

  public ColorRGB(Color c) {
    red = c.getRed();
    green = c.getGreen();
    blue = c.getBlue();
  }

  public int toInt() {
    return ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
  }

  public ColorRGB set(ColorRGB x) {
    red = x.red;
    green = x.green;
    blue = x.blue;
    return this;
  }

  public void set(int r, int g, int b) {
    red = r;
    green = g;
    blue = b;
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
    red *= f;
    green *= f;
    blue *= f;
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
    return "(" + red + "," + green + "," + blue + ")";
  }
  
  public int getRed() { return red; }
  public int getGreen() { return green; }
  public int getBlue() { return blue; }
  
  public static ColorRGB parse(String arg0) throws NumberFormatException, InvalidParameterException {
	  if(arg0==null) throw new InvalidParameterException("arg0 is null");
	  if(arg0.startsWith("#")) arg0 = arg0.substring(1);
	  int size = arg0.length();
	  if(size>8 || size<6) throw new InvalidParameterException("arg0 must be 6 or 8 characters long in hex format.");
	  if(size>6) arg0.substring(0,6);
	  
	  int r = (int) Long.parseLong(arg0, 16);
	  return new ColorRGB(r);
  }
}
