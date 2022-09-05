package com.marginallyclever.robotoverlord.swinginterface.undoableedits;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class AddEntityEdit extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Entity entity;
	private ArrayList<Entity> previouslyPickedEntities;	
	private RobotOverlord ro;
	
	public AddEntityEdit(RobotOverlord ro,Entity entity) {
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
		ro.getScene().addChild(entity);
		ro.updateEntityTree();
		ArrayList<Entity> list = new ArrayList<Entity>();
		list.add(entity);
		ro.updateSelectEntities(list);
		previouslyPickedEntities = ro.getSelectedEntities();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		ro.getScene().removeChild(entity);
		ro.updateEntityTree();
		ro.updateSelectEntities(previouslyPickedEntities);
	}
}
