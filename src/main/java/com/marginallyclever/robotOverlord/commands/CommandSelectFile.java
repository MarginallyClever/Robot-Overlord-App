package com.marginallyclever.robotOverlord.commands;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.actions.ActionSelectFile;

public class CommandSelectFile extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionSelectFile actionSelectFile;
	private String oldFilename,newFilename;
	private String label;
	
	public CommandSelectFile(ActionSelectFile actionSelectFile,String label,String newFilename) {
		this.actionSelectFile = actionSelectFile;
		this.newFilename = newFilename;
		this.oldFilename = actionSelectFile.getFilename();
		this.label = label;
		setFilename(newFilename);
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
		return "choose "+label;
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
		setFilename(newFilename);
	}

	@Override
	public void undo() throws CannotUndoException {
		setFilename(oldFilename);
	}

	private void setFilename(String filename) {
		actionSelectFile.setFilename(filename);
	}
}
