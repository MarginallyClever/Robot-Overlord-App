package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.parameters.AbstractParameter;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Some {@link Component}s have parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 */
public class AbstractParameterEdit<T> extends AbstractUndoableEdit {
	private final AbstractParameter<T> parameter;
	private final T oldValue, newValue;
	
	public AbstractParameterEdit(AbstractParameter<T> parameter, T newValue) {
		super();
		
		this.parameter = parameter;
		this.newValue = newValue;
		this.oldValue = parameter.get();

		parameter.set(newValue);
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("AbstractParameterEdit.name", parameter.getName());
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		parameter.set(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		parameter.set(oldValue);
	}
	
	/**
	 * sequential changes to the same entity will be merged into a single undo/redo change.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if( anEdit instanceof AbstractParameterEdit<?>) {
			AbstractParameterEdit<T> b = (AbstractParameterEdit<T>) anEdit;
			if( b.parameter == this.parameter) return true;
		}
		return super.addEdit(anEdit);
	}
}
