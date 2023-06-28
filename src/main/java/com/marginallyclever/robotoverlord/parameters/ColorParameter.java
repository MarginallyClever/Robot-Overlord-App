package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
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

	public ColorParameter(String name, double r, double g, double b, double a) {
		super(name,new double[] {r,g,b,a});
	}

	public ColorParameter(String name) {
		this(name,0,0,0,0);
	}

	public ColorParameter() {
		this("Color");
	}

	public void set(double r,double g,double b,double a) {
		super.set(new double[] {r,g,b,a});
	}

	@Override
	public void set(double [] newValue) {
		set(newValue[0],newValue[1],newValue[2],newValue[3]);		
	}

	public double getR() { return get()[0]; }
	public double getG() { return get()[1]; }
	public double getB() { return get()[2]; }
	public double getA() { return get()[3]; }
	
	public float [] getFloatArray() {
		double [] value = get();
		return new float[] {
				(float) value[0],
				(float) value[1],
				(float) value[2],
				(float) value[3] };
	}
	
	public double[] getDoubleArray() {
		return get().clone();
	}
	
	@Override
	public String toString() {
		return getName()+"="+Arrays.toString(get());
	}
	
	@Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		double[] rgba = get();
		jo.put("r", rgba[0]);
		jo.put("g", rgba[1]);
		jo.put("b", rgba[2]);
		jo.put("a", rgba[3]);
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
		super.parseJSON(jo,context);
		double[] rgba = get();
		rgba[0] = jo.getDouble("r");
		rgba[1] = jo.getDouble("g");
		rgba[2] = jo.getDouble("b");
		rgba[3] = jo.getDouble("a");
		set(rgba);
	}

	public int getHex() {
		double[] rgba = get();
		int r = (int)(rgba[0]*255);
		int g = (int)(rgba[1]*255);
		int b = (int)(rgba[2]*255);
		int a = (int)(rgba[3]*255);
		return (r<<24) | (g<<16) | (b<<8) | a;
	}

	public void setFromHex(int hex) {
		double[] rgba = get();
		rgba[0] = ((hex>>24)&0xFF)/255.0;
		rgba[1] = ((hex>>16)&0xFF)/255.0;
		rgba[2] = ((hex>> 8)&0xFF)/255.0;
		rgba[3] = ((hex    )&0xFF)/255.0;
		set(rgba);
	}
}
