package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ActionChangeAbstractEntity<T> extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AbstractEntity<T> e;
	private T oldValue,newValue;
	
	public ActionChangeAbstractEntity(AbstractEntity<T> e,T newValue) {
		super();
		
		this.e = e;
		this.newValue = newValue;
		this.oldValue = e.get();

		e.set(newValue);
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("Change ")+e.getName();
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
