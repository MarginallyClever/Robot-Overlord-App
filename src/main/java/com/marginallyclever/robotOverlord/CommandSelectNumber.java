package com.marginallyclever.robotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class CommandSelectNumber extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionSelectNumber actionSelectNumber;
	private float oldValue,newValue;
	
	public CommandSelectNumber(ActionSelectNumber actionSelectNumber,float newValue) {
		this.actionSelectNumber = actionSelectNumber;
		this.newValue = newValue;
		this.oldValue = actionSelectNumber.getValue();
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
		return "choose file";
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

	private void setValue(float value) {
		actionSelectNumber.setValue(value);
	}
}
