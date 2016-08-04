package com.marginallyclever.robotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class CommandAddEntity extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private Entity previouslyPickedEntity;	
	private RobotOverlord ro;
	
	public CommandAddEntity(RobotOverlord ro,Entity entity) {
		this.entity = entity;
		this.ro = ro;
		addNow();
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return "Add "+entity.getDisplayName();
	}


	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		addNow();
	}

	@Override
	public void undo() throws CannotUndoException {
		removeNow();
	}

	private void addNow() {
		ro.getWorld().addObject(entity);
		previouslyPickedEntity = ro.getPickedEntity(); 
		ro.setContextMenu(entity);
	}
	
	private void removeNow() {
		ro.getWorld().removeObject(entity);
		ro.setContextMenu(previouslyPickedEntity);
	}
}
