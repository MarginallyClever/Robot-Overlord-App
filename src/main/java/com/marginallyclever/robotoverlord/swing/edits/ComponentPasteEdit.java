package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.ComponentFactory;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import org.json.JSONObject;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.LinkedList;
import java.util.List;

/**
 * Paste a component into a list of entities.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ComponentPasteEdit extends AbstractUndoableEdit {
    private final String name;
    private final Component copiedComponent;
    private final List<Entity> parents = new LinkedList<>();
    private final List<Component> copies = new LinkedList<>();

    public ComponentPasteEdit(String name, Component copiedComponent, List<Entity> parents) {
        super();
        this.name = name;
        this.copiedComponent = copiedComponent;
        this.parents.addAll(parents);
        doIt();
    }

    @Override
    public String getPresentationName() {
        return name;
    }

    private void doIt() {
        copies.clear();
        SerializationContext context = new SerializationContext("");

        JSONObject serialized = copiedComponent.toJSON(context);
        for(Entity parent : parents) {
            if(parent.containsAnInstanceOfTheSameClass(copiedComponent)) {
                // TODO add a warning to the user
                continue;
            }
            Component copy = ComponentFactory.load(copiedComponent.getClass().getName());
            parent.addComponent(copy);
            copy.parseJSON(serialized,context);
            copies.add(copy);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for(Entity parent : parents) {
            for(Component copy : copies) {
                parent.removeComponent(copy);
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
