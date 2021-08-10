package com.marginallyclever.robotOverlord.uiExposedTypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * each color component is in the range [0...1]
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ColorEntity extends AbstractEntity<float[]> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2162459650636178933L;


	public ColorEntity() {
		this(0,0,0,0);
	}

	public ColorEntity(String name,double r,double g,double b,double a) {
		super(new float[] {(float)r,(float)g,(float)b,(float)a});
		setName(name);
	}
	
	public ColorEntity(double r,double g,double b,double a) {
		super(new float[] {(float)r,(float)g,(float)b,(float)a});
	}

	public void set(double r,double g,double b,double a) {
		super.set(new float[] {(float)r,(float)g,(float)b,(float)a});
	}

	@Override
	public void set(float [] newValue) {
		set(newValue[0],newValue[1],newValue[2],newValue[3]);		
	}

	public double getR() { return t[0]; }
	public double getG() { return t[1]; }
	public double getB() { return t[2]; }
	public double getA() { return t[3]; }
	
	public float[] getFloatArray() {
		return t.clone();
	}
	
	public double[] getDoubleArray() {
		return new double[]{
				t[0],
				t[1],
				t[2],
				t[3]
				};
	}
	
	@Override
	public String toString() {
		return getName()+"="+"("
				+ StringHelper.formatFloat(t[0]) + ","
				+ StringHelper.formatFloat(t[1]) + ","
				+ StringHelper.formatFloat(t[2]) + ","
				+ StringHelper.formatFloat(t[3])
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
