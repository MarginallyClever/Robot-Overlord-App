package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class EntityAddEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
	private final EntityManager entityManager;
	private final Entity child;
	private final Entity parent;
	
	public EntityAddEdit(EntityManager entityManager, Entity parent, Entity child) {
		super();
		this.entityManager = entityManager;
		this.child = child;
		this.parent = parent;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Add ")+ child.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		entityManager.addEntityToParent(child,parent);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entityManager.removeEntityFromParent(child,parent);
	}
}
