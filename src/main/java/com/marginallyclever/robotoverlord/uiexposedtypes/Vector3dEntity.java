package com.marginallyclever.robotoverlord.uiexposedtypes;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Vector3dEntity extends AbstractEntity<Vector3d> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6504746583968180431L;


	public Vector3dEntity() {
		super(new Vector3d());
	}
	
	public Vector3dEntity(String name) {
		super(new Vector3d());
		setName(name);
	}
	
	public Vector3dEntity(String name,Vector3d b) {
		super(b);
		setName(name);
	}
	
	public Vector3dEntity(double x,double y,double z) {
		super(new Vector3d(x,y,z));
	}
	
	public Vector3dEntity(String name,double x,double y,double z) {
		super(new Vector3d(x,y,z));
		setName(name);
	}
	
	public void set(double x,double y,double z) {
		t.set(x,y,z);
	}
	
	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	
	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view. 
	 * @param view the panel to decorate
	 */
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		super.save(writer);
		writer.write("value=" + get()+",\n");
	}

	@Override
	public void load(BufferedReader reader) throws Exception {
		super.load(reader);
		String str = reader.readLine();
		if(str.endsWith(",")) str = str.substring(0,str.length()-1);
		if(!str.startsWith("value=(")) throw new IOException("Expected 'value=(' at start but found "+str.substring(0,10));
		if(!str.endsWith(")")) throw new IOException("Expected ')' at end but found "+str.substring(str.length()-10));

		String [] tok = str.substring(7,str.length()-1).split(", ");

		if(tok.length!=3) throw new IOException("Expected 3 parameters, found "+tok.length);

		double [] result = new double[tok.length];
		for(int i=0;i< result.length;++i) {
			result[i] = Double.parseDouble(tok[i]);
		}
		set(result[0],result[1],result[2]);
	}
}
