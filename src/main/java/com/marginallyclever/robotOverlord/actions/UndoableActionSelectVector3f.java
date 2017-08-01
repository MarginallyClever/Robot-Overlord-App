package com.marginallyclever.robotOverlord.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectVector3f;

/**
 * Undoable action to select a Vector3f.
 * <p>
 * Some Entities have Vector3f (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectVector3f extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectVector3f actionSelectVector3f;
	private Vector3f oldValue,newValue;
	private String label;
	
	public UndoableActionSelectVector3f(UserCommandSelectVector3f actionSelectVector3f,String label,Vector3f newValue) {
		this.actionSelectVector3f = actionSelectVector3f;
		this.label = label;
		this.newValue = new Vector3f(newValue);
		this.oldValue = new Vector3f(actionSelectVector3f.getValue());
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

	private void setValue(Vector3f value) {
		actionSelectVector3f.setValue(value);
	}
}
