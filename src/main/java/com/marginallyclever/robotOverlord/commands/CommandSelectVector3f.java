package com.marginallyclever.robotOverlord.commands;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.actions.ActionSelectVector3f;

public class CommandSelectVector3f extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionSelectVector3f actionSelectVector3f;
	private Vector3f oldValue,newValue;
	
	public CommandSelectVector3f(ActionSelectVector3f actionSelectVector3f,Vector3f newValue) {
		this.actionSelectVector3f = actionSelectVector3f;
		this.newValue = new Vector3f(newValue);
		this.oldValue = new Vector3f(actionSelectVector3f.getValue());
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
		return "change xyz";
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

	private void setValue(Vector3f value) {
		actionSelectVector3f.setValue(value);
	}
}
