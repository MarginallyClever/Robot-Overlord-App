package com.marginallyclever.robotOverlord.swingInterface.undoableEdits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.marginallyclever.robotOverlord.AbstractEntity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Undoable action to select a boolean.
 * <p>
 * Some Entities have string (text) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class AbstractEntityEdit<T> extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AbstractEntity<T> entity;
	private T oldValue,newValue;
	
	public AbstractEntityEdit(AbstractEntity<T> entity,T newValue) {
		super();
		
		this.entity = entity;
		this.newValue = newValue;
		this.oldValue = entity.get();

		entity.set(newValue);
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("Change ")+entity.getName();
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
	
	/**
	 * sequential changes to the same entity will be merged into a single undo/redo change.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if( anEdit instanceof AbstractEntityEdit<?> ) {
			AbstractEntityEdit<T> b = (AbstractEntityEdit<T>) anEdit;
			if( b.entity == this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
