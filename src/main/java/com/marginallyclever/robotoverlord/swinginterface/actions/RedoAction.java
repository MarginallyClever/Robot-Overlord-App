package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;

/**
 * go forward one step in the undo/redo history.
 * @author Dan Royer
 */
public class RedoAction extends AbstractAction {
	/**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;
	private final UndoManager undo;
	private UndoAction undoCommand;
	
    public RedoAction(UndoManager undo) {
        super(Translator.get("RedoAction.name"));
        this.undo = undo;
        setEnabled(false);

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
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