package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable action to change the currently selected entity.
 * This is the equivalent to moving the caret in a text document.
 * @author Dan Royer
 *
 */
public class ActionEntityRename extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Entity e;
	private String oldName ="";
	private String newName ="";
	
	public ActionEntityRename(Entity e,String newName) {
		super();
		
		this.newName = newName;
		this.oldName = e.getName();
		this.e=e;
		e.setName(newName);
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Rename ")+oldName;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		e.setName(newName);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		e.setName(oldName);
	}
}
