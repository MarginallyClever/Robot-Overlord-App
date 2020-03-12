package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectBoolean;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectBoolean extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectBoolean actionSelectBoolean;
	private Boolean oldValue,newValue;
	private String label;
	
	public UndoableActionSelectBoolean(UserCommandSelectBoolean actionSelectBoolean,String label,Boolean newValue) {
		super();
		
		this.actionSelectBoolean = actionSelectBoolean;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = actionSelectBoolean.getValue();
		
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
		actionSelectBoolean.setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		actionSelectBoolean.setValue(oldValue);
	}
}
