package com.marginallyclever.robotoverlord.swinginterface.undoableedits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;

import java.io.Serial;

/**
 * An undoable command to make a physical entity move.
 *  
 * @author Dan Royer
 *
 */
public class MoveEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
		
	private final Entity entity;
	private final Matrix4d next;
	private final Matrix4d prev;
	
	/**
	 * 
	 * @param entity who
	 * @param newPose where
	 */
	public MoveEdit(Entity entity, Matrix4d newPose) {
		super();
		
		this.entity = entity;
		this.prev = entity.getComponent(PoseComponent.class).getWorld();
		this.next = newPose;

		entity.getComponent(PoseComponent.class).setWorld(next);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		entity.getComponent(PoseComponent.class).setWorld(next);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.getComponent(PoseComponent.class).setWorld(next);
	}
	
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if(anEdit instanceof MoveEdit) {
			MoveEdit APEM = (MoveEdit)anEdit;
			if(APEM.entity==this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
