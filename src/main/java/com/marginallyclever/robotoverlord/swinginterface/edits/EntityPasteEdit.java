package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import org.json.JSONObject;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.LinkedList;
import java.util.List;

public class EntityPasteEdit extends AbstractUndoableEdit {
    private final String name;
    private final RobotOverlord ro;
    private final Entity copiedEntities;
    private final List<Entity> parents;
    private final List<Entity> copies = new LinkedList<>();

    public EntityPasteEdit(String name, RobotOverlord ro, Entity copiedEntities, List<Entity> parents) {
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
        copies.clear();
        List<Entity> from = copiedEntities.getEntities();
        for(Entity e : from) {
            JSONObject serialized = e.toJSON();
            for(Entity parent : parents) {
                Entity copy = new Entity();
                parent.addEntity(copy);
                copy.parseJSON(serialized);
                copies.add(copy);
            }
        }
        ro.setSelectedEntity(null);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for(Entity parent : parents) {
            for(Entity copy : copies) {
                parent.removeEntity(copy);
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
