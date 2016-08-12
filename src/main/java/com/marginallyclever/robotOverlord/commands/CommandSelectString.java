package com.marginallyclever.robotOverlord.commands;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.actions.ActionSelectString;

public class CommandSelectString extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionSelectString actionSelectString;
	private String oldValue,newValue;
	
	public CommandSelectString(ActionSelectString actionSelectString,String newValue) {
		this.actionSelectString = actionSelectString;
		this.newValue = newValue;
		this.oldValue = actionSelectString.getValue();
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
		return "change text";
	}


	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		setValue(oldValue);
	}

	private void setValue(String value) {
		actionSelectString.setValue(value);
	}
}
