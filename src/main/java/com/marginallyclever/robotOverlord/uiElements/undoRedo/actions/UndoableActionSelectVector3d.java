package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

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
	private Vector3dEntity e;
	private Vector3d oldValue,newValue;
	
	public UndoableActionSelectVector3d(Vector3dEntity e,Vector3d newValue) {
		super();
		
		this.e = e;
		
		this.newValue = new Vector3d(newValue);
		this.oldValue = new Vector3d(e.get());

		e.set(newValue);
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("change ")+e.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		e.set(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		e.set(oldValue);
	}
}
