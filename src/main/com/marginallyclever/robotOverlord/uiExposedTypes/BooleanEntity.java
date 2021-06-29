package com.marginallyclever.robotOverlord.uiExposedTypes;

import com.marginallyclever.robotOverlord.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class BooleanEntity extends AbstractEntity<Boolean> {
	/**
	 * 
	 */
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

	public String toString() {
		return getName()+"="+t.toString();
	}
	
	public void toggle() {
		set(!get());
	}
	
	
	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view. 
	 * @param g
	 */
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}
}
