package com.marginallyclever.robotOverlord.uiExposedTypes;

import com.marginallyclever.robotOverlord.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class IntEntity extends AbstractEntity<Integer> {
	/**
	 * 
	 */
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
}
