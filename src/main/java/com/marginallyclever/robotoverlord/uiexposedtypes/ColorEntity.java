package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

/**
 * each color component is in the range [0...1]
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ColorEntity extends AbstractEntity<double[]> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2162459650636178933L;


	public ColorEntity() {
		this(0,0,0,0);
	}

	public ColorEntity(String name,double r,double g,double b,double a) {
		super(new double[] {r,g,b,a});
		setName(name);
	}
	
	public ColorEntity(double r,double g,double b,double a) {
		super(new double[] {r,g,b,a});
	}

	public void set(double r,double g,double b,double a) {
		super.set(new double[] {r,g,b,a});
	}

	@Override
	public void set(double [] newValue) {
		set(newValue[0],newValue[1],newValue[2],newValue[3]);		
	}

	public double getR() { return t[0]; }
	public double getG() { return t[1]; }
	public double getB() { return t[2]; }
	public double getA() { return t[3]; }
	
	public float [] getFloatArray() {
		return new float[] {
				(float)t[0],
				(float)t[1],
				(float)t[2],
				(float)t[3] };
	}
	
	public double[] getDoubleArray() {
		return t.clone();
	}
	
	@Override
	public String toString() {
		return getName()+"="+"("
				+ StringHelper.formatDouble(t[0]) + ","
				+ StringHelper.formatDouble(t[1]) + ","
				+ StringHelper.formatDouble(t[2]) + ","
				+ StringHelper.formatDouble(t[3])
				+")";
	}
	
	
	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view. 
	 * @param g
	 */
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}
}
