package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class UndoableActionAddEntity extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private Entity previouslyPickedEntity;	
	private RobotOverlord ro;
	
	public UndoableActionAddEntity(RobotOverlord ro,Entity entity) {
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
		return Translator.get("Add ")+entity.getName();
	}


	@Override
	public String getRedoPresentationName() {
		return Translator.get("Redo ") + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return Translator.get("Undo ") + getPresentationName();
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
		ro.getWorld().addChild(entity);
		previouslyPickedEntity = ro.getPickedEntity(); 
		ro.pickEntity(entity);
	}
	
	private void removeNow() {
		ro.getWorld().removeEntity(entity);
		ro.pickEntity(previouslyPickedEntity);
	}
}
