package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class BooleanParameterEdit extends AbstractParameterEdit<Boolean> {
	public BooleanParameterEdit(AbstractParameter<Boolean> e, Boolean newValue) {
		super(e, newValue);
	}
}
