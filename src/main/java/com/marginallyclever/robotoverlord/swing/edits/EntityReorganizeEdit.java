package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An undoable action to move one or more {@link Entity} from one parent to another.
 * @author Dan Royer
 *
 */
public class EntityReorganizeEdit extends AbstractUndoableEdit {
	private final EntityManager entityManager;
	private final Map<Entity,Entity> childParent = new HashMap<>();
	private final Entity newParent;

	public EntityReorganizeEdit(EntityManager entityManager, List<Entity> children, Entity newParent) {
		super();
		this.entityManager = entityManager;
		this.newParent = newParent;

		for(Entity child : children) {
			childParent.put(child,child.getParent());
		}

		doIt();
	}

	public EntityReorganizeEdit(EntityManager entityManager, Entity child, Entity newParent) {
		super();
		this.entityManager = entityManager;
		this.newParent = newParent;

		childParent.put(child,child.getParent());

		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("EntityReorganizeEdit.name") + getFancyName();
	}

	private String getFancyName() {
		if(childParent.size()==1) {
			return childParent.values().iterator().next().getName();
		} else {
			return Integer.toString(childParent.size());
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		for(Entity child : childParent.keySet()) {
			Entity oldParent = childParent.get(child);
			entityManager.removeEntityFromParent(child,oldParent);
			entityManager.addEntityToParent(child,newParent);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(Entity child : childParent.keySet()) {
			Entity oldParent = childParent.get(child);
			entityManager.removeEntityFromParent(child,newParent);
			entityManager.addEntityToParent(child,oldParent);
		}
	}
}
