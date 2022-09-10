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
