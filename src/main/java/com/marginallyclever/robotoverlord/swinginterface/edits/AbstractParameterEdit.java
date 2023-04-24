package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.io.Serial;

/**
 * Some {@link com.marginallyclever.robotoverlord.Component}s have parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 */
public class AbstractParameterEdit<T> extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
	private final AbstractParameter<T> entity;
	private final T oldValue,newValue;
	
	public AbstractParameterEdit(AbstractParameter<T> entity, T newValue) {
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
		if( anEdit instanceof AbstractParameterEdit<?>) {
			AbstractParameterEdit<T> b = (AbstractParameterEdit<T>) anEdit;
			if( b.entity == this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
