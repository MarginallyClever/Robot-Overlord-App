package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectColorRGBA;

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
	private UserCommandSelectColorRGBA actionSelectColorRGBA;
	private float [] oldValue, newValue;
	private String label;
	
	public UndoableActionSelectColorRGBA(UserCommandSelectColorRGBA actionSelectColorRGBA,String label,float [] newValue) {
		super();
		
		this.actionSelectColorRGBA = actionSelectColorRGBA;
		this.label = label;
		this.newValue = newValue.clone();
		this.oldValue = actionSelectColorRGBA.getValue().clone();
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("change ")+label;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		actionSelectColorRGBA.setValue(newValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		actionSelectColorRGBA.setValue(oldValue);
	}
}
