package com.marginallyclever.robotOverlord.swingInterface.actions;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ActionChangeBoolean extends ActionChangeAbstractEntity<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActionChangeBoolean(AbstractEntity<Boolean> e, Boolean newValue) {
		super(e, newValue);
	}
}
