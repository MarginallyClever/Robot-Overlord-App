package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * each color component is in the range [0...1]
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ColorEntity extends Entity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8719587212293927388L;
	
	private double r,g,b,a;

	public ColorEntity() {
		super();
		set(0,0,0,0);
	}

	public ColorEntity(String name,double r,double g,double b,double a) {
		super();
		setName(name);
		set(r,g,b,a);
	}
	public ColorEntity(double r,double g,double b,double a) {
		super();
		set(r,g,b,a);
	}

	public void set(double r,double g,double b,double a) {
		if(hasChanged()) return;
		setChanged();
		this.r=r;
		this.g=g;
		this.b=b;
		this.a=a;
		notifyObservers();
	}

	public double getR() { return r; }
	public double getG() { return g; }
	public double getB() { return b; }
	public double getA() { return a; }
	
	public float[] getFloatArray() {
		return new float[]{
			(float)r,
			(float)g,
			(float)b,
			(float)a
		};
	}
	
	public double[] getDoubleArray() {
		return new double[]{r,g,b,a};
	}
	
	public String toString() {
		return getName()+"="+"("
				+ StringHelper.formatDouble(r) + ","
				+ StringHelper.formatDouble(g) + ","
				+ StringHelper.formatDouble(b) + ","
				+ StringHelper.formatDouble(a)
				+")";
	}
}
