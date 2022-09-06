package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DoubleEntity extends AbstractEntity<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3278660733983538798L;

	public DoubleEntity(String s) {
		super(0.0);
		setName(s);
	}
	
	public DoubleEntity(String s,double d) {
		super(d);
		setName(s);
	}

	public DoubleEntity(String s,float d) {
		super((double)d);
		setName(s);
	}
	
	public DoubleEntity(String s,int d) {
		super((double)d);
		setName(s);
	}
	
	@Override
	public String toString() {
		return getName()+"="+StringHelper.formatDouble(t);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		super.save(writer);
		writer.write("value=" + get().toString()+",\n");
	}

	@Override
	public void load(BufferedReader reader) throws Exception {
		super.load(reader);
		String str = reader.readLine();
		if(str.endsWith(",")) str = str.substring(0,str.length()-1);
		if(!str.startsWith("value=")) throw new IOException("Expected 'value=' but found "+str.substring(0,10));
		set(Double.parseDouble(str.substring(6)));
	}
}
