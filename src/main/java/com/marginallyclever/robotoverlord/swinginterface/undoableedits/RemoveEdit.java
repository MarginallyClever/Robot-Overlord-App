package com.marginallyclever.robotoverlord.swinginterface.undoableedits;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Removable;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class RemoveEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
	private final Entity entity;
	private final Entity parent;
	private final RobotOverlord ro;

	public RemoveEdit(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;
		this.parent = entity.getParent();

		doIt();
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("Remove ")+entity.getName();
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
