package com.marginallyclever.robotOverlord.uiExposedTypes;

import com.marginallyclever.robotOverlord.AbstractEntity;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class StringEntity extends AbstractEntity<String> {
	/**
	 * 
	 */
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
}
