package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectBoolean;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectBoolean extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectBoolean actionSelectBoolean;
	private Boolean oldValue,newValue;
	private String label;
	
	public UndoableActionSelectBoolean(UserCommandSelectBoolean actionSelectBoolean,String label,Boolean newValue) {
		this.actionSelectBoolean = actionSelectBoolean;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = actionSelectBoolean.getValue();
		setValue(newValue);
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return Translator.get("change ")+label;
	}


	@Override
	public String getRedoPresentationName() {
		return Translator.get("Redo ") + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return Translator.get("Undo ") + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		setValue(oldValue);
	}

	private void setValue(Boolean value) {
		actionSelectBoolean.setValue(value);
	}
}
