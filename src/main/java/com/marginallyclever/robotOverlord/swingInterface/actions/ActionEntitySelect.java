package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable action to change the currently selected entity.
 * This is the equivalent to moving the caret in a text document.
 * @author Dan Royer
 *
 */
public class ActionEntitySelect extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Entity next;
	private ArrayList<Entity> prev;	
	private RobotOverlord ro;
	
	public ActionEntitySelect(RobotOverlord ro,ArrayList<Entity> prev,Entity next) {
		super();
		
		this.ro = ro;
		this.next=next;
		this.prev=prev;
		ArrayList<Entity> ent = new ArrayList<Entity>();
		ent.add(next);
		ro.selectEntities(ent);
	}

	@Override
	public String getPresentationName() {
		String name = (next==null) ? Translator.get("nothing") : next.getName();
		return Translator.get("Select ")+name;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		ArrayList<Entity> ent = new ArrayList<Entity>();
		ent.add(next);
		ro.selectEntities(ent);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		ro.selectEntities(prev);
	}
}
