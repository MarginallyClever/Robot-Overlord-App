package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectVector3d;

/**
 * Undoable action to select a Vector3d.
 * <p>
 * Some Entities have Vector3d (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectVector3d extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectVector3d actionSelectVector3d;
	private Vector3d oldValue,newValue;
	private String label;
	
	public UndoableActionSelectVector3d(UserCommandSelectVector3d actionSelectVector3d,String label,Vector3d newValue) {
		this.actionSelectVector3d = actionSelectVector3d;
		this.label = label;
		this.newValue = new Vector3d(newValue);
		this.oldValue = new Vector3d(actionSelectVector3d.getValue());
		setValue(newValue);
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
		return Translator.get("change ")+label;
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
		setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		setValue(oldValue);
	}

	private void setValue(Vector3d value) {
		actionSelectVector3d.setValue(value);
	}
}
