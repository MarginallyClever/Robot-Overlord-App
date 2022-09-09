package com.marginallyclever.robotoverlord.swinginterface.edits;

import java.io.Serial;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class AddEntityEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Entity entity;
	private final Entity parent;
	private List<Entity> previouslyPickedEntities;
	
	public AddEntityEdit(Entity parent,Entity entity) {
		super();
		
		this.entity = entity;
		this.parent = parent;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Add ")+entity.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		parent.addEntity(entity);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		parent.removeChild(entity);
	}
}
