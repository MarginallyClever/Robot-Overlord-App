package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

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
	private final Map<Entity,Entity> childParent = new HashMap<>();
	private final Entity newParent;

	public EntityReorganizeEdit(List<Entity> children, Entity newParent) {
		super();
		this.newParent = newParent;

		for(Entity child : children) {
			childParent.put(child,child.getParent());
		}

		doIt();
	}

	public EntityReorganizeEdit(Entity child, Entity newParent) {
		super();
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
		System.out.println("Reorganizing "+getFancyName());
		for(Entity child : childParent.keySet()) {
			child.getParent().removeEntity(child);
			newParent.addEntity(child);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(Entity child : childParent.keySet()) {
			newParent.removeEntity(child);
			childParent.get(child).addEntity(child);
		}
	}
}
