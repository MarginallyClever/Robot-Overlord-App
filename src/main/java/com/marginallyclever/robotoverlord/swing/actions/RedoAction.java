package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * go forward one step in the undo/redo history.
 * @author Dan Royer
 */
public class RedoAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(RedoAction.class);
	private final UndoManager undoManager;
	private UndoAction undoAction;
	
    public RedoAction(UndoManager undoManager) {
        super(Translator.get("RedoAction.name"));
        this.undoManager = undoManager;
        setEnabled(false);

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