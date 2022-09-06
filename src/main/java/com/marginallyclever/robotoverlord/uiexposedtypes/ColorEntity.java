package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;
import java.util.Arrays;

/**
 * each color component is in the range [0...1]
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ColorEntity extends AbstractEntity<double[]> {
	@Serial
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
		return getName()+"="+Arrays.toString(get());
	}
	
	
	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view. 
	 * @param view the panel to fill.
	 */
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		super.save(writer);
		writer.write("value=" + Arrays.toString(get())+",\n");
	}

	@Override
	public void load(BufferedReader reader) throws Exception {
		super.load(reader);
		String str = reader.readLine();
		if(str.endsWith(",")) str = str.substring(0,str.length()-1);
		if(!str.startsWith("value=[")) throw new IOException("Expected 'value=[' at start but found "+str.substring(0,10));
		if(!str.endsWith("]")) throw new IOException("Expected ']' at end but found "+str.substring(str.length()-10));

		String [] tok = str.substring(7,str.length()-1).split(", ");

		if(tok.length!=4) throw new IOException("Expected 4 parameters, found "+tok.length);

		double [] result = new double[tok.length];
		for(int i=0;i< result.length;++i) {
			result[i] = Double.parseDouble(tok[i]);
		}
		set(result);
	}
}
