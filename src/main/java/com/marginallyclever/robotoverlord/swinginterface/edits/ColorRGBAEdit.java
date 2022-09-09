package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.uiexposedtypes.ColorEntity;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Undoable action to select a ColorRGBA.
 * <p>
 * Some Entities have ColorRGBA (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ColorRGBAEdit extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ColorEntity entity;
	private double [] newValue;
	private double [] oldValue;
	
	public ColorRGBAEdit(ColorEntity entity,double [] newValue) {
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
		if(anEdit instanceof ColorRGBAEdit ) {
			ColorRGBAEdit APEM = (ColorRGBAEdit)anEdit;
			if(APEM.entity == this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
