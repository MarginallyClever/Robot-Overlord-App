package com.marginallyclever.robotOverlord.swingInterface.actions;

import com.marginallyclever.robotOverlord.AbstractEntity;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ActionChangeString extends ActionChangeAbstractEntity<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActionChangeString(AbstractEntity<String> e, String newValue) {
		super(e, newValue);
	}
}
