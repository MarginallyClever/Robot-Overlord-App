package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

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
	private BooleanEntity entity;
	private Boolean oldValue,newValue;
	private String label;
	
	public UndoableActionSelectBoolean(BooleanEntity entity,String label,Boolean newValue) {
		super();
		
		this.entity = entity;
		this.label = label;
		this.newValue = newValue;
		this.oldValue = entity.get();

		entity.set(newValue);
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("change ")+label;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		entity.set(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.set(oldValue);
	}
}
