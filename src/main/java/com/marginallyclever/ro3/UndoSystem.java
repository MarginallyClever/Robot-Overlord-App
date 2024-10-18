package com.marginallyclever.ro3;

import com.marginallyclever.ro3.apps.actions.RedoAction;
import com.marginallyclever.ro3.apps.actions.UndoAction;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

/**
 * {@link UndoSystem} is a singleton to manage the undo/redo history and associated {@link javax.swing.AbstractAction}s.
 */
public class UndoSystem {
	private static final UndoManager undoManager = new UndoManager();
	private static final UndoAction commandUndo = new UndoAction(undoManager);
	private static final RedoAction commandRedo = new RedoAction(undoManager);

	/**
	 * Start the undo system.  This is called by the main frame after the menu bar is created.
	 */
	public static void start() {
        commandUndo.setRedoCommand(commandRedo);
    	commandRedo.setUndoAction(commandUndo);
	}
	
	public static UndoAction getCommandUndo() {
		return commandUndo;
	}

	public static RedoAction getCommandRedo() {
		return commandRedo;
	}

	public static void addEvent(AbstractUndoableEdit edit) {
		undoManager.addEdit(edit);
		commandUndo.updateUndoState();
	}

	public static void reset() {
		undoManager.discardAllEdits();
		commandUndo.updateUndoState();
		commandRedo.updateRedoState();
	}
}
