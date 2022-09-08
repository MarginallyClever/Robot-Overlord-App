package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class PasteEdit extends AbstractUndoableEdit {
    private final String name;
    private final RobotOverlord ro;
    private final Entity copy;

    public PasteEdit(String name, RobotOverlord ro, Entity toPaste) throws Exception {
        super();
        this.name = name;
        this.ro = ro;
        this.copy = toPaste.deepCopy();
        doIt();
    }

    @Override
    public String getPresentationName() {
        return name;
    }

    private void doIt() {
        ro.getScene().addChild(copy);
        ro.setSelectedEntity(copy);
    }

    @Override
    public void undo() throws CannotUndoException {
        ro.getScene().removeChild(copy);
        ro.setSelectedEntity(null);
        super.undo();
    }

    @Override
    public void redo() throws CannotRedoException {
        doIt();
        super.redo();
    }
}
