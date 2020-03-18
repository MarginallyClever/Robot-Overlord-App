package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Undoable action to select a ColorRGBA.
 * <p>
 * Some Entities have ColorRGBA (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ActionChangeColorRGBA extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ColorEntity entity;
	private float [] newValue;
	private float [] oldValue;
	
	public ActionChangeColorRGBA(ColorEntity entity,float [] newValue) {
		super();
		
		this.entity = entity;
		this.newValue = newValue.clone();
		this.oldValue = entity.getFloatArray();

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
		if(anEdit instanceof ActionChangeColorRGBA ) {
			ActionChangeColorRGBA APEM = (ActionChangeColorRGBA)anEdit;
			if(APEM.entity == this.entity) return true;
		}
		return super.addEdit(anEdit);
	}
}
