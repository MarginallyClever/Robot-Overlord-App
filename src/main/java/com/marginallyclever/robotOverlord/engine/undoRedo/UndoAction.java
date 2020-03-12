package com.marginallyclever.robotOverlord.engine.undoRedo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class UndoAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UndoManager undo;
	private RedoAction redoAction;
	
    public UndoAction(UndoManager undo) {
        super("Undo");
    	this.undo=undo;
        setEnabled(false);
    }

	@Override
    public void actionPerformed(ActionEvent e) {
        try {
            undo.undo();
        } catch (CannotUndoException ex) {
            System.out.println("Unable to undo: " + ex);
            ex.printStackTrace();
        }
        updateUndoState();
        if(redoAction!=null) redoAction.updateRedoState();
    }

    public void updateUndoState() {
        if (undo.canUndo()) {
            setEnabled(true);
            putValue(Action.NAME, undo.getUndoPresentationName());
        } else {
            setEnabled(false);
            putValue(Action.NAME, "Undo");
        }
    }
    
    public void setRedoAction(RedoAction redoAction) {
    	this.redoAction=redoAction;
    }
}
