package com.marginallyclever.ro3.node.nodefactory;

import com.marginallyclever.ro3.node.Node;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A factory that can be used to create Nodes.  It does not manage the objects it creates.
 */
public class NodeFactory {
    private static final Logger logger = LoggerFactory.getLogger(NodeFactory.class);

    /**
     * Categories of Node types.  These categories can be nested in a tree.
     */
    public static class Category {
        private final String name;
        private final Supplier<Node> supplier;
        private final List<Category> children = new ArrayList<>();
        private Category parent=null;

        public Category(String name,Supplier<Node> supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public void add(Category c) {
            children.add(c);
            c.parent = this;
        }

        public Category add(String name, Supplier<Node> supplier) {
            Category item = new Category(name,supplier);
            add(item);
            return item;
        }

        public String getName() {
            return name;
        }

        public Category getParent() {
            return parent;
        }

        public List<Category> getChildren() {
            return children;
        }

        public Supplier<Node> getSupplier() {
            return supplier;
        }
    }

    private final Category root = new Category("Node",Node::new);

    public NodeFactory() {
        super();
        //scan();
    }

    public Category getRoot() {
        return root;
    }

    public Supplier<Node> getSupplierFor(String path) {
        List<Category> toCheck = new ArrayList<>();
        toCheck.add(root);
        while(!toCheck.isEmpty()) {
            Category current = toCheck.remove(0);
            toCheck.addAll(current.children);

            if(current.name.equals(path)) {
                return current.supplier;
            }
        }

        return null;
    }

    public Node create(String path) {
        Supplier<Node> supplier = getSupplierFor(path);
        if(supplier==null) return null;
        return supplier.get();
    }

    public void scan() {
        // Create a new instance of Reflections
        Reflections reflections = new Reflections("com.marginallyclever.ro3");
        // Get all classes that extend T
        Set<Class<? extends Node>> found = reflections.getSubTypesOf(Node.class);
        // Now, classes contains all classes that extend T
        for (Class<? extends Node> clazz : found) {
            logger.info("Found " + clazz.getName());
        }
    }

    public void clear() {
        root.children.clear();
    }
}
