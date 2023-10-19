package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class StringParameterEdit extends AbstractParameterEdit<String> {
	public StringParameterEdit(AbstractParameter<String> parameter, String newValue) {
		super(parameter, newValue);
	}
}
