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
 * go back one step in the undo/redo history.
 * @author Dan Royer
 */
public class UndoAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(UndoAction.class);
	private final UndoManager undoManager;
	private RedoAction redoAction;
	
    public UndoAction(UndoManager undoManager) {
        super(Translator.get("UndoAction.name"));
    	this.undoManager = undoManager;
        setEnabled(false);
        
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
