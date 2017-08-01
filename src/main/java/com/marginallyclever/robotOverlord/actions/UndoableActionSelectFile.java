package com.marginallyclever.robotOverlord.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;

/**
 * Undoable action to select a file.
 * <p>
 * Some Entities have file parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectFile extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectFile actionSelectFile;
	private String oldFilename,newFilename;
	private String label;
	
	public UndoableActionSelectFile(UserCommandSelectFile actionSelectFile,String label,String newFilename) {
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
		return Translator.get("choose ")+label;
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
