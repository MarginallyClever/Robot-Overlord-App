package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.swinginterface.actions.RedoAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.UndoAction;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;

public class UndoSystem {
	private static final UndoManager undoManager = new UndoManager();
	private static final UndoAction commandUndo = new UndoAction(undoManager);
	private static final RedoAction commandRedo = new RedoAction(undoManager);
	
	public void start() {
        commandUndo.setRedoCommand(commandRedo);
    	commandRedo.setUndoCommand(commandUndo);
	}
	
	public static UndoAction getCommandUndo() {
		return commandUndo;
	}

	public static RedoAction getCommandRedo() {
		return commandRedo;
	}

	public static void addEvent(Object src, AbstractUndoableEdit edit) {
		undoManager.undoableEditHappened(new UndoableEditEvent(src,edit));
		getCommandUndo().updateUndoState();
		getCommandRedo().updateRedoState();
	}
}
