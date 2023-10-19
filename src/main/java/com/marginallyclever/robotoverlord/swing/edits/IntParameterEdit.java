package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

/**
 * Some {@link Component}s have
 * {@link com.marginallyclever.robotoverlord.parameters.IntParameter}s.  This class ensures changing those parameters
 * is undoable.
 *  
 * @author Dan Royer
 *
 */
public class IntParameterEdit extends AbstractParameterEdit<Integer> {
	public IntParameterEdit(AbstractParameter<Integer> e, Integer newValue) {
		super(e, newValue);
	}
}
