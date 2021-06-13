package com.marginallyclever.robotOverlord.swingInterface.undoableEdits;

import com.marginallyclever.robotOverlord.AbstractEntity;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class BooleanEdit extends AbstractEntityEdit<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BooleanEdit(AbstractEntity<Boolean> e, Boolean newValue) {
		super(e, newValue);
	}
}
