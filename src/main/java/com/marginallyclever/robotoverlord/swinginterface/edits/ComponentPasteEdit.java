package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.ComponentFactory;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import org.json.JSONObject;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.LinkedList;
import java.util.List;

public class ComponentPasteEdit extends AbstractUndoableEdit {
    private final String name;
    private final RobotOverlord ro;
    private final Component copiedComponent;
    private final List<Entity> parents = new LinkedList<>();
    private final List<Component> copies = new LinkedList<>();

    public ComponentPasteEdit(String name, RobotOverlord ro, Component copiedComponent, List<Entity> parents) {
        super();
        this.name = name;
        this.ro = ro;
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

        JSONObject serialized = copiedComponent.toJSON();
        for(Entity parent : parents) {
            if(parent.containsAnInstanceOfTheSameClass(copiedComponent)) {
                // TODO add a warning to the user
                continue;
            }
            Component copy = ComponentFactory.load(copiedComponent.getClass().getName());
            parent.addComponent(copy);
            copy.parseJSON(serialized);
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
        ro.setSelectedEntity(null);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        doIt();
    }
}
