package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectComboBox;

/**
 * Undoable action to select a string.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectComboBox extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectComboBox actionSelectComboBox;
	private int oldValue,newValue;
	private String label;
	
	public UndoableActionSelectComboBox(UserCommandSelectComboBox actionSelectComboBox,String label,int newValue) {
		super();
		
		this.actionSelectComboBox = actionSelectComboBox;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = actionSelectComboBox.getIndex();
		
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
		actionSelectComboBox.setIndex(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		actionSelectComboBox.setIndex(oldValue);
	}
}
