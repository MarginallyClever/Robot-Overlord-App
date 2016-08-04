package com.marginallyclever.robotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class CommandSelectFile extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionSelectFile actionSelectFile;
	private String oldFilename,newFilename;
	
	public CommandSelectFile(ActionSelectFile actionSelectFile,String newFilename) {
		this.actionSelectFile = actionSelectFile;
		this.newFilename = newFilename;
		this.oldFilename = actionSelectFile.getFilename();
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
