package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An undoable action to move an {@link Entity} from one parent to another.
 * @author Dan Royer
 *
 */
public class EntityMoveEdit extends AbstractUndoableEdit {
	private final Map<Entity,Entity> childParent = new HashMap<>();
	private final RobotOverlord ro;
	private final String name;

	public EntityMoveEdit(String name, RobotOverlord ro, List<Entity> entityList) {
		super();
		this.name = name;
		this.ro = ro;

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
			System.out.println("Removing "+child.getFullPath());
			child.getParent().removeEntity(child);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(Entity child : childParent.keySet()) {
			childParent.get(child).addEntity(child);
		}
	}
}
