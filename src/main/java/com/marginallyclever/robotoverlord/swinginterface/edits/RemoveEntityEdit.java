package com.marginallyclever.robotoverlord.swinginterface.edits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class RemoveEntityEdit extends AbstractUndoableEdit {
	private final Map<Entity,Entity> childParent = new HashMap<>();
	private final RobotOverlord ro;
	private final String name;

	public RemoveEntityEdit(String name,RobotOverlord ro, List<Entity> entityList) {
		super();
		this.name = name;
		this.ro = ro;

		for(Entity entity : entityList) {
			childParent.put(entity,entity.getParent());
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
		for(Entity entity : childParent.keySet()) {
			entity.getParent().removeChild(entity);
		}
		ro.updateEntityTree();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(Entity entity : childParent.keySet()) {
			childParent.get(entity).addEntity(entity);
		}
		ro.updateEntityTree();
	}
}
