package com.marginallyclever.robotoverlord.swinginterface.edits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ComboBoxEdit extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IntEntity e;
	private int oldValue,newValue;
	private String label;
	
	public ComboBoxEdit(IntEntity e,String label,int newValue) {
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
