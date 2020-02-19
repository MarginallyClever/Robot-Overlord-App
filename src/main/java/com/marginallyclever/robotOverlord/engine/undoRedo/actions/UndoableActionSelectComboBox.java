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
		this.actionSelectComboBox = actionSelectComboBox;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = actionSelectComboBox.getIndex();
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
		this.setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		this.setValue(oldValue);
	}

	private void setValue(int value) {
		actionSelectComboBox.setIndex(value);
	}
}
