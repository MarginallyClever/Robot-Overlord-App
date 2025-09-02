package com.marginallyclever.ro3.node.nodefactory;

import com.marginallyclever.ro3.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Categories of {@link Node} types.  These categories can be nested in a tree.
 */
public class NodeFactoryCategory {
    private final String name;
    private final Supplier<Node> supplier;
    private final List<NodeFactoryCategory> children = new ArrayList<>();
    private NodeFactoryCategory parent = null;

    public NodeFactoryCategory(String name, Supplier<Node> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public void clear() {
        children.clear();
        parent = null;
    }

    public void add(NodeFactoryCategory c) {
        children.add(c);
        c.parent = this;
    }

    public NodeFactoryCategory add(String name, Supplier<Node> supplier) {
        NodeFactoryCategory item = new NodeFactoryCategory(name, supplier);
        add(item);
        return item;
    }

    public String getName() {
        return name;
    }

    public NodeFactoryCategory getParent() {
        return parent;
    }

    public List<NodeFactoryCategory> getChildren() {
        return children;
    }

    public Supplier<Node> getSupplier() {
        return supplier;
    }
}
