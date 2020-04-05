package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;
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
	private static final long serialVersionUID = -5939946851374643084L;
	
	public IntEntity() {
		super();
	}
	
	public IntEntity(String name,int d) {
		super(d);
		setName(name);
	}
	
	public String toString() {
		return getName()+"="+t.toString();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}
}
