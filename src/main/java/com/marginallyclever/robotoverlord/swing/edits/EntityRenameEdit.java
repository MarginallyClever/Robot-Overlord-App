package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.entity.EntityManagerEvent;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

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
	private final EntityManager entityManager;
	
	public EntityRenameEdit(Entity entity, String newName, EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
		this.entity = entity;
		this.newName = newName;
		this.oldName = entity.getName();
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("ComponentRenameEdit.name",oldName);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}

	protected void doIt() {
		entity.setName(newName);
		entityManager.fireEntityManagerEvent(new EntityManagerEvent(EntityManagerEvent.ENTITY_RENAMED,entity,null));
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.setName(oldName);
	}
}
