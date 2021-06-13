package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

import com.marginallyclever.convenience.log.Log;

/**
 * go forward one step in the undo/redo history.
 * @author Dan Royer
 */
public class RedoAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UndoManager undo;
	private UndoAction undoCommand;
	
    public RedoAction(UndoManager undo) {
        super("Redo");
        this.undo = undo;
        setEnabled(false);

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            undo.redo();
        } catch (CannotRedoException ex) {
            Log.message("Unable to redo: " + ex);
            ex.printStackTrace();
        }
        updateRedoState();
        if(undoCommand!=null) undoCommand.updateUndoState();
    }

    public void updateRedoState() {
        if (undo.canRedo()) {
            setEnabled(true);
            putValue(Action.NAME, undo.getRedoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Redo");
        }
    }
    
    public void setUndoCommand(UndoAction undoCommand) {
    	this.undoCommand=undoCommand;
    }
}