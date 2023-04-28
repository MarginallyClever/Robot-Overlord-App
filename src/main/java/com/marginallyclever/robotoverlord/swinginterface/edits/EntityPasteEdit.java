package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import org.json.JSONObject;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.LinkedList;
import java.util.List;

public class EntityPasteEdit extends AbstractUndoableEdit {
    private final String name;
    private final Scene scene;
    private final Entity copiedEntities;
    private final List<Entity> parents;
    private final List<Entity> copies = new LinkedList<>();

    public EntityPasteEdit(String name, Scene scene, Entity copiedEntities, List<Entity> parents) {
        super();
        this.name = name;
        this.copiedEntities = copiedEntities;
        this.parents = parents;
        this.scene = scene;
        doIt();
    }

    @Override
    public String getPresentationName() {
        return name;
    }

    private void doIt() {
        copies.clear();
        List<Entity> from = copiedEntities.getChildren();
        for(Entity e : from) {
            JSONObject serialized = e.toJSON();
            for(Entity parent : parents) {
                Entity copy = new Entity();
                scene.addEntityToParent(copy, parent);
                copy.parseJSON(serialized);
                copies.add(copy);
            }
        }
        Clipboard.setSelectedEntity(null);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for(Entity parent : parents) {
            for(Entity copy : copies) {
                scene.removeEntityFromParent(copy, parent);
            }
        }
        Clipboard.setSelectedEntity(null);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        doIt();
    }
}
