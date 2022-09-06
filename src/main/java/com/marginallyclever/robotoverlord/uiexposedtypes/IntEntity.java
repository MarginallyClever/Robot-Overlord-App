package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serial;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class IntEntity extends AbstractEntity<Integer> {
	@Serial
	private static final long serialVersionUID = -665400072120969645L;

	public IntEntity() {
		super();
	}
	
	public IntEntity(String name,int d) {
		super(d);
		setName(name);
	}
	
	@Override
	public String toString() {
		return getName()+"="+t.toString();
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
		set(Integer.parseInt(str.substring(6)));
	}
}
