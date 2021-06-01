package com.marginallyclever.robotOverlord.uiExposedTypes;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

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
	
	public String toString() {
		return getName()+"="+StringHelper.formatDouble(t);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.add(this);
	}
}
