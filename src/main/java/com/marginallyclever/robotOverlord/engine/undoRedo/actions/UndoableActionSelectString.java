package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectString;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectString extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectString actionSelectString;
	private String oldValue,newValue;
	private String label;
	
	public UndoableActionSelectString(UserCommandSelectString actionSelectString,String label,String newValue) {
		super();
		
		this.actionSelectString = actionSelectString;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = actionSelectString.getValue();
		
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
		actionSelectString.setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		actionSelectString.setValue(oldValue);
	}
}
