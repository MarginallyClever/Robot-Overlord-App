package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class EntityDeleteEdit extends AbstractUndoableEdit {
	private static final Logger logger = LoggerFactory.getLogger(EntityDeleteEdit.class);
	private final EntityManager entityManager;
	private final Map<Entity,Entity> childParent = new HashMap<>();
	private final String name;

	public EntityDeleteEdit(String name, EntityManager entityManager, List<Entity> entityList) {
		super();
		this.name = name;
		this.entityManager = entityManager;

		for(Entity child : entityList) {
			childParent.put(child,child.getParent());
		}

		doIt();
	}

	@Override
	public String getPresentationName() {
		return name;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		for(Entity child : childParent.keySet()) {
			logger.info("Removing "+child.getFullPath());
			Entity parent = childParent.get(child);
			entityManager.removeEntityFromParent(child,parent);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(Entity child : childParent.keySet()) {
			Entity parent = childParent.get(child);
			entityManager.addEntityToParent(child,parent);
		}
	}
}
