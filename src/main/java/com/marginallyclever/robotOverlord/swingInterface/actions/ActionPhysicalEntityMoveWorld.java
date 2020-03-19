package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;

/**
 * An undoable command to make a physical entity move.
 *  
 * @author Dan Royer
 *
 */
public class ActionPhysicalEntityMoveWorld extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private PoseEntity entity;
	private Matrix4d next;
	private Matrix4d prev;
	
	/**
	 * 
	 * @param robot which machine
	 * @param axis index of axis
	 * @param direction 1 or -1
	 */
	public ActionPhysicalEntityMoveWorld(PoseEntity entity,Matrix4d newPose) {
		super();
		
		this.entity = entity;
		this.prev = entity.getPoseWorld();
		this.next = newPose;

		entity.setPoseWorld(next);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		entity.setPoseWorld(next);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.setPoseWorld(prev);
	}
	
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if(anEdit instanceof ActionPhysicalEntityMoveWorld) {
			ActionPhysicalEntityMoveWorld APEM = (ActionPhysicalEntityMoveWorld)anEdit;
			if(APEM.entity==this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
