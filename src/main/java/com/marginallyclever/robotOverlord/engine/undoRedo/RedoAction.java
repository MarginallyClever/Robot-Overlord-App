package com.marginallyclever.robotOverlord.engine.undoRedo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

public class RedoAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UndoManager undo;
	private UndoAction undoAction;
	
    public RedoAction(UndoManager undo) {
        super("Redo");
        this.undo = undo;
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            undo.redo();
        } catch (CannotRedoException ex) {
            System.out.println("Unable to redo: " + ex);
            ex.printStackTrace();
        }
        updateRedoState();
        if(undoAction!=null) undoAction.updateUndoState();
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
    
    public void setUndoAction(UndoAction undoAction) {
    	this.undoAction=undoAction;
    }
}