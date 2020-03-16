package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

/**
 * Undoable action to select a ColorRGBA.
 * <p>
 * Some Entities have ColorRGBA (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectColorRGBA extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ColorEntity e;
	private float [] newValue;
	private float [] oldValue;
	
	public UndoableActionSelectColorRGBA(ColorEntity e,float [] newValue) {
		super();
		
		this.e = e;
		this.newValue = newValue.clone();
		this.oldValue = e.getFloatArray();

		e.set(newValue);
	}

	@Override
	public String getPresentationName() {
		return Translator.get("change ")+e.getName();
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
