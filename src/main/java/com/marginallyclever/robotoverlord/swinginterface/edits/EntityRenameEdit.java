package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;

/**
 * An undoable action to change the currently selected entity.
 * This is the equivalent to moving the caret in a text document.
 * @author Dan Royer
 *
 */
public class EntityRenameEdit extends AbstractUndoableEdit {
	private final Entity entity;
	private final String oldName;
	private final String newName;
	
	public EntityRenameEdit(Entity entity, String newName) {
		super();
		this.entity = entity;
		this.newName = newName;
		this.oldName = entity.getName();
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Rename ")+oldName;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}

	protected void doIt() {
		entity.setName(newName);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.setName(oldName);
	}
}
