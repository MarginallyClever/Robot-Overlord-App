package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectString extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StringEntity e;
	private String oldValue,newValue;
	
	public UndoableActionSelectString(StringEntity e,String newValue) {
		super();
		
		this.e = e;
		this.newValue = newValue;
		this.oldValue = e.get();

		e.set(newValue);
	}

	@Override
	public String getPresentationName() {
		return Translator.get("change ")+e.getName();
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
