package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

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
		super();
		
		this.entity = entity;
		this.ro = ro;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Add ")+entity.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		ro.getWorld().addChild(entity);
		ro.pickEntity(entity);
		previouslyPickedEntity = ro.getPickedEntity();
		ro.updateEntityTree();
		ro.pickEntity(entity);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		ro.getWorld().removeChild(entity);
		ro.updateEntityTree();
		ro.pickEntity(previouslyPickedEntity);
	}
}
