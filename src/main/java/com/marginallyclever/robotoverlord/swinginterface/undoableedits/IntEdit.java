package com.marginallyclever.robotoverlord.swinginterface.undoableedits;

import com.marginallyclever.robotoverlord.AbstractEntity;

/**
 * Undoable action to select a number.
 * <p>
 * Some Entities have decimal number (float) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class IntEdit extends AbstractEntityEdit<Integer> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public IntEdit(AbstractEntity<Integer> e, Integer newValue) {
		super(e, newValue);
	}
}
