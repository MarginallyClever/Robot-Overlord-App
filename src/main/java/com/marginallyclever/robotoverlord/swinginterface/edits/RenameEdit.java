package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * An undoable action to change the currently selected entity.
 * This is the equivalent to moving the caret in a text document.
 * @author Dan Royer
 *
 */
public class RenameEdit extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Entity e;
	private String oldName ="";
	private String newName ="";
	private RobotOverlord ro;
	
	public RenameEdit(RobotOverlord ro,Entity e,String newName) {
		super();
		
		this.ro = ro;
		this.newName = newName;
		this.oldName = e.getName();
		this.e=e;
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Rename ")+oldName;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}

	protected void doIt() {
		e.setName(newName);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		e.setName(oldName);
	}
}
