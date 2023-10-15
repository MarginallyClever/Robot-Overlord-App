package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ComboBoxEdit extends AbstractUndoableEdit {
	private final IntParameter e;
	private final int oldValue,newValue;
	private final String label;
	
	public ComboBoxEdit(IntParameter e, String label, int newValue) {
		super();
		
		this.e = e;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = e.get();
		e.set(newValue);
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("change ")+label;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		e.set(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		e.set(oldValue);
	}
}
