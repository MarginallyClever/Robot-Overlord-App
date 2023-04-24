package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

import java.io.Serial;

/**
 * Some {@link com.marginallyclever.robotoverlord.Component}s have
 * {@link com.marginallyclever.robotoverlord.parameters.IntParameter}s.  This class ensures changing those parameters
 * is undoable.
 *  
 * @author Dan Royer
 *
 */
public class IntParameterEdit extends AbstractParameterEdit<Integer> {
	@Serial
	private static final long serialVersionUID = 1L;
	
	public IntParameterEdit(AbstractParameter<Integer> e, Integer newValue) {
		super(e, newValue);
	}
}
