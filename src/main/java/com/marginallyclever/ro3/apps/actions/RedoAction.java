package com.marginallyclever.ro3.apps.actions;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * go forward one step in the undo/redo history.
 * @author Dan Royer
 */
public class RedoAction extends AbstractAction {
	private final UndoManager undoManager;
	private UndoAction undoAction;
	
    public RedoAction(UndoManager undoManager) {
        super();
        this.undoManager = undoManager;
        setEnabled(false);
        putValue(NAME, "Redo");
        putValue(SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-redo-16.png"))));
        putValue(SHORT_DESCRIPTION, "Redo the last action.");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.redo();
        if(undoAction!=null) undoAction.updateUndoState();
        updateRedoState();
    }

    public void updateRedoState() {
        if (undoManager.canRedo()) {
            setEnabled(true);
            putValue(NAME, undoManager.getRedoPresentationName());
        } else {
            setEnabled(false);
            putValue(NAME, "Redo");
        }
    }
    
    public void setUndoAction(UndoAction undoAction) {
    	this.undoAction = undoAction;
    }
}