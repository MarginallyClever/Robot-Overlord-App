package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
public class BooleanEntity extends AbstractEntity<Boolean> {
	@Serial
	private static final long serialVersionUID = 1393466347571351227L;

	public BooleanEntity() {
		super();
		setName("Boolean");
	}
	
	public BooleanEntity(String name, boolean b) {
		super();
		setName(name);
		set(b);
	}

	@Override
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	public void toggle() {
		set(!get());
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
		writer.write("value=" + get().toString()+",\n");
	}

	@Override
	public void load(BufferedReader reader) throws Exception {
		super.load(reader);
		String str = reader.readLine();
		if(str.endsWith(",")) str = str.substring(0,str.length()-1);
		if(!str.startsWith("value=")) throw new IOException("Expected 'value=' but found "+str.substring(0,10));
		set(Boolean.parseBoolean(str.substring(6)));
	}
}
