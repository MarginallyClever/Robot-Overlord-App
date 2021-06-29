package com.marginallyclever.robotOverlord.swingInterface.undoableEdits;

import com.marginallyclever.robotOverlord.AbstractEntity;

/**
 * Undoable action to select a number.
 * <p>
 * Some Entities have decimal number (float) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class DoubleEdit extends AbstractEntityEdit<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DoubleEdit(AbstractEntity<Double> e, Double newValue) {
		super(e, newValue);
	}
}
