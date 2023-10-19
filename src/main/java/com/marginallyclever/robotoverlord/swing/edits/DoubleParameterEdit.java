package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.parameters.AbstractParameter;

/**
 * Some {@link Component}s have
 * {@link com.marginallyclever.robotoverlord.parameters.DoubleParameter}.
 * This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class DoubleParameterEdit extends AbstractParameterEdit<Double> {
	public DoubleParameterEdit(AbstractParameter<Double> e, Double newValue) {
		super(e, newValue);
	}
}
