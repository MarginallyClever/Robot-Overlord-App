package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.LinkedList;
import java.util.List;

public class PasteEntityEdit extends AbstractUndoableEdit {
    private final String name;
    private final RobotOverlord ro;
    private final Entity copiedEntities;
    private final List<Entity> parents;
    private final List<Entity> copies = new LinkedList<>();

    public PasteEntityEdit(String name, RobotOverlord ro, Entity copiedEntities,List<Entity> parents) {
        super();
        this.name = name;
        this.ro = ro;
        this.copiedEntities = copiedEntities;
        this.parents = parents;
        doIt();
    }

    @Override
    public String getPresentationName() {
        return name;
    }

    private void doIt() {
        for(Entity parent : parents) {
            List<Entity> from = copiedEntities.getEntities();
            for(Entity e : from) {
                Entity copy = e.deepCopy();
                copies.add(copy);
                parent.addEntity(copy);
            }
        }
        ro.setSelectedEntities(copies);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for(Entity parent : parents) {
            for(Entity copy : copies) {
                parent.removeChild(copy);
            }
        }
        ro.setSelectedEntity(null);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        doIt();
    }
}
