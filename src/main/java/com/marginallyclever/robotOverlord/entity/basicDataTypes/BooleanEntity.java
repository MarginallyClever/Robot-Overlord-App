package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class BooleanEntity extends AbstractEntity<Boolean> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public BooleanEntity() {
		super();
		setName("Boolean");
	}
	
	public BooleanEntity(String name, boolean b) {
		super();
		setName(name);
		set(b);
	}

	public String toString() {
		return getName()+"="+t.toString();
	}
	
	public void toggle() {
		set(!get());
	}
}
