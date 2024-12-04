package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;

import javax.swing.undo.AbstractUndoableEdit;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * <p>Use a {@link com.marginallyclever.ro3.node.nodefactory.NodeFactory} to add a new instance of a {@link Node} to
 * every selected branch of the scene tree.</p>
 * @param <T> the type of node to add
 */
public class AddNode<T extends Node> extends AbstractUndoableEdit {
    private final Supplier<T> factory;
    private final List<Node> created = new ArrayList<>();

    public AddNode(Supplier<T> factory) {
        this.factory = factory;
        execute();
    }

    @Override
    public String getPresentationName() {
        return "Add "+factory.get().getClass().getSimpleName();
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    public void execute() {
        addChildrenUsingFactory(factory);
    }

    public void addChildrenUsingFactory(Supplier<T> factory) {
        if(factory==null) throw new InvalidParameterException("factory is null");

        var list = new ArrayList<>(Registry.selection.getList());
        if(list.isEmpty()) {
            addOne(Registry.getScene());
        } else {
            for(Node parent : list) {
                addOne(parent);
            }
        }
    }

    public void addOne(Node parent) {
        Node child = factory.get();
        created.add(child);
        parent.addChild(child);
    }

    @Override
    public void undo() {
        super.undo();
        for(Node child : created) {
            Node parent = child.getParent();
            parent.removeChild(child);
        }
    }
}