package com.marginallyclever.robotOverlord.engine.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectFile;

/**
 * Undoable action to select a file.
 * <p>
 * Some Entities have file parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionSelectFile extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserCommandSelectFile actionSelectFile;
	private String oldFilename,newFilename;
	private String label;
	
	public UndoableActionSelectFile(UserCommandSelectFile actionSelectFile,String label,String newFilename) {
		super();
		
		this.actionSelectFile = actionSelectFile;
		this.newFilename = newFilename;
		this.oldFilename = actionSelectFile.getFilename();
		this.label = label;
		
		doIt();
	}
	
	@Override
	public String getPresentationName() {
		return Translator.get("choose ")+label;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		actionSelectFile.setFilename(newFilename);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		actionSelectFile.setFilename(oldFilename);
	}
}
