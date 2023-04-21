package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

import java.io.Serial;

/**
 * Undoable action to select a number.
 * <p>
 * Some Entities have decimal number (float) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class IntEdit extends AbstractEntityEdit<Integer> {
	@Serial
	private static final long serialVersionUID = 1L;
	
	public IntEdit(AbstractParameter<Integer> e, Integer newValue) {
		super(e, newValue);
	}
}
