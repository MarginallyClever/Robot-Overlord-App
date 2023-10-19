package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Some {@link Component} have {@link ColorParameter}.
 * This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ColorParameterEdit extends AbstractUndoableEdit {
	private final ColorParameter entity;
	private final double [] newValue;
	private final double [] oldValue;
	
	public ColorParameterEdit(ColorParameter entity, double [] newValue) {
		super();
		
		this.entity = entity;
		this.newValue = newValue.clone();
		this.oldValue = entity.getDoubleArray();

		entity.set(newValue);
	}

	@Override
	public String getPresentationName() {
		return Translator.get("change ")+entity.getName();
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
	
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if(anEdit instanceof ColorParameterEdit) {
			ColorParameterEdit APEM = (ColorParameterEdit)anEdit;
			if(APEM.entity == this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
