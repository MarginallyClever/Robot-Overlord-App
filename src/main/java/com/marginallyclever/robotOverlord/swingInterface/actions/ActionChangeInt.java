package com.marginallyclever.robotOverlord.swingInterface.actions;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * Undoable action to select a number.
 * <p>
 * Some Entities have decimal number (float) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ActionChangeInt extends ActionChangeAbstractEntity<Integer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ActionChangeInt(AbstractEntity<Integer> e, Integer newValue) {
		super(e, newValue);
	}
}
