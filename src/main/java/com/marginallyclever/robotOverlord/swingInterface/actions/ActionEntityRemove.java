package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Removable;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable action to remove an {@link Entity} from the world.
 * @author Dan Royer
 *
 */
public class ActionEntityRemove extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private Entity parent;
	private RobotOverlord ro;
	
	public ActionEntityRemove(RobotOverlord ro,Entity entity) {
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
		if(entity instanceof Removable) {
			((Removable)entity).beingRemoved();
		}
		if(parent!=null) parent.removeChild(entity);
		ro.updateEntityTree();
		ro.selectEntities(null);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if(parent!=null) parent.addChild(entity);
		ro.updateEntityTree();
		ArrayList<Entity> list = new ArrayList<Entity>();
		list.add(entity);
		ro.selectEntities(list);
	}
}
