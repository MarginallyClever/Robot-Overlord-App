package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class StringEntity extends AbstractEntity<String> {
	@Serial
	private static final long serialVersionUID = 4182705695944961446L;

	public StringEntity() {
		super("","");
	}
	
	// ambiguous solution is to do both!
	public StringEntity(String t) {
		super();
		setName(t);
    	this.t = t;
	}
	
	public StringEntity(String name, String value) {
		super();
		setName(name);
		t=value;
	}

	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		super.save(writer);
		writer.write("value=\"" + get().toString()+"\",\n");
	}

	@Override
	public void load(BufferedReader reader) throws Exception {
		super.load(reader);
		String str = reader.readLine();
		if(str.endsWith(",")) str = str.substring(0,str.length()-1);
		if(!str.startsWith("value=\"")) throw new IOException("Expected 'value=\"' at start but found "+str.substring(0,10));
		if(!str.endsWith("\"")) throw new IOException("Expected '\"' at end but found "+str.substring(str.length()-10));
		set(str.substring(7,str.length()-1));
	}
}
