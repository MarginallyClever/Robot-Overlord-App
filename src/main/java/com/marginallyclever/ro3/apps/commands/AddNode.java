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
 * every selected {@link Node}.</p>
 * @param <T> the type of node to add
 */
public class AddNode<T extends Node> extends AbstractUndoableEdit {
    private final Supplier<T> factory;
    private final List<Node> created = new ArrayList<>();
    private final List<Node> parents = new ArrayList<>();

    /**
     * <p>Use a {@link com.marginallyclever.ro3.node.nodefactory.NodeFactory} to add a new instance of a {@link Node}
     * to every selected {@link Node}.</p>
     * @param factory the factory to use
     */
    public AddNode(Supplier<T> factory) {
        super();
        this.factory = factory;
        parents.addAll(Registry.selection.getList());
        execute();
    }

    /**
     * <p>Use a {@link com.marginallyclever.ro3.node.nodefactory.NodeFactory} to add a new instance of a {@link Node}
     * to a single parent {@link Node}.</p>
     * @param factory the factory to use
     * @param parent the parent to add the new node to
     */
    public AddNode(Supplier<T> factory, Node parent) {
        super();
        this.factory = factory;
        parents.add(parent);
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

        if(parents.isEmpty()) {
            addOne(Registry.getScene());
        } else {
            for(Node parent : parents) {
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

    public Node getFirstCreated() {
        return created.get(0);
    }
}