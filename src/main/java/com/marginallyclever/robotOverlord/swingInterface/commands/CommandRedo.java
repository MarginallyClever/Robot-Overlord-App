package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

/**
 * go forward one step in the undo/redo history.
 * @author Dan Royer
 */
public class CommandRedo extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UndoManager undo;
	private CommandUndo undoCommand;
	
    public CommandRedo(UndoManager undo) {
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
            System.out.println("Unable to redo: " + ex);
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
    
    public void setUndoCommand(CommandUndo undoCommand) {
    	this.undoCommand=undoCommand;
    }
}