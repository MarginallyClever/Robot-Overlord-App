package com.marginallyclever.robotoverlord.swinginterface.edits;

import java.io.Serial;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class RemoveEntityEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
	private final Entity entity;
	private final Entity parent;
	private final RobotOverlord ro;

	public RemoveEntityEdit(RobotOverlord ro, Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;
		this.parent = entity.getParent();

		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Remove ") + entity.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		if(parent!=null) parent.removeChild(entity);
		ro.updateEntityTree();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if(parent!=null) parent.addChild(entity);
		ro.updateEntityTree();
	}
}
