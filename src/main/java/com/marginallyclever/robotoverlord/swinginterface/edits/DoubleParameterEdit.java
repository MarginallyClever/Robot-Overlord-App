package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

import java.io.Serial;

/**
 * Some {@link com.marginallyclever.robotoverlord.Component}s have
 * {@link com.marginallyclever.robotoverlord.parameters.DoubleParameter}.
 * This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class DoubleParameterEdit extends AbstractParameterEdit<Double> {
	@Serial
	private static final long serialVersionUID = 1L;

	public DoubleParameterEdit(AbstractParameter<Double> e, Double newValue) {
		super(e, newValue);
	}
}
