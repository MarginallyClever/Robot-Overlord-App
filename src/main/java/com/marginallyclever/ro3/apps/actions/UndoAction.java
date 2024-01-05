package com.marginallyclever.ro3.apps.actions;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * go back one step in the undo/redo history.
 * @author Dan Royer
 */
public class UndoAction extends AbstractAction {
	private final UndoManager undoManager;
	private RedoAction redoAction;
	
    public UndoAction(UndoManager undoManager) {
        super();
    	this.undoManager = undoManager;
        setEnabled(false);

        putValue(NAME, "Undo");
        putValue(SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-undo-16.png"))));
        putValue(SHORT_DESCRIPTION, "Undo the last action.");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
    }

	@Override
    public void actionPerformed(ActionEvent e) {
        undoManager.undo();
        if(redoAction!=null) redoAction.updateRedoState();
        updateUndoState();
    }

    public void updateUndoState() {
        if (undoManager.canUndo()) {
            setEnabled(true);
            putValue(NAME, undoManager.getUndoPresentationName());
        } else {
            setEnabled(false);
            putValue(NAME, "Undo");
        }
    }
    
    public void setRedoCommand(RedoAction redoCommand) {
    	this.redoAction=redoCommand;
    }
}
