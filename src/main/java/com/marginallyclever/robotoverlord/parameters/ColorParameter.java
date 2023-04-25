package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * each color component is in the range [0...1]
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ColorParameter extends AbstractParameter<double[]> {
	public ColorParameter() {
		this(0,0,0,0);
	}

	public ColorParameter(String name, double r, double g, double b, double a) {
		super(new double[] {r,g,b,a});
		setName(name);
	}
	
	public ColorParameter(double r, double g, double b, double a) {
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
		return getName()+"="+Arrays.toString(get());
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject jo = super.toJSON();
		double[] rgba = get();
		jo.put("r", rgba[0]);
		jo.put("g", rgba[1]);
		jo.put("b", rgba[2]);
		jo.put("a", rgba[3]);
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo) throws JSONException {
		super.parseJSON(jo);
		double[] rgba = get();
		rgba[0] = jo.getDouble("r");
		rgba[1] = jo.getDouble("g");
		rgba[2] = jo.getDouble("b");
		rgba[3] = jo.getDouble("a");
		set(rgba);
	}
}
