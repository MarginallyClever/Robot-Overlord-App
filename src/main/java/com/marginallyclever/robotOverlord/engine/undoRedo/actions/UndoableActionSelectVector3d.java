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
		super();
		
		this.actionSelectVector3d = actionSelectVector3d;
		this.label = label;
		this.newValue = new Vector3d(newValue);
		this.oldValue = new Vector3d(actionSelectVector3d.getValue());
		
		doIt();
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("change ")+label;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		actionSelectVector3d.setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		actionSelectVector3d.setValue(oldValue);
	}
}
