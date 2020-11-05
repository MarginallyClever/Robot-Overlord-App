package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class ActionEntityAdd extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Entity entity;
	private ArrayList<Entity> previouslyPickedEntities;	
	private RobotOverlord ro;
	
	public ActionEntityAdd(RobotOverlord ro,Entity entity) {
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
		ro.updateEntityTree();
		ArrayList<Entity> list = new ArrayList<Entity>();
		list.add(entity);
		ro.selectEntities(list);
		previouslyPickedEntities = ro.getSelectedEntities();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		ro.getWorld().removeChild(entity);
		ro.updateEntityTree();
		ro.selectEntities(previouslyPickedEntities);
	}
}
