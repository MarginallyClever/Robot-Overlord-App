package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class UndoableActionRemoveEntity extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private RobotOverlord ro;
	
	public UndoableActionRemoveEntity(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;

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
		ro.getWorld().removeChild(entity);
		ro.updateEntityTree();
		ro.pickEntity(null);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		ro.getWorld().addChild(entity);
		ro.updateEntityTree();
		ro.pickEntity(entity);
	}
}
