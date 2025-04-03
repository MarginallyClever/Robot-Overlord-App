package com.marginallyclever.ro3.node.nodefactory;

import com.marginallyclever.ro3.node.Node;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * find the sub-factory that matches the given identifier.
     * @param identifier the name of type of {@link Node} the sub-factory produces.
     * @return the sub-factory that matches the given identifier, or null if not found.
     */
    public Supplier<Node> getSupplierFor(String identifier) {
        List<Category> toCheck = new ArrayList<>();
        toCheck.add(root);
        while(!toCheck.isEmpty()) {
            Category current = toCheck.removeFirst();
            if(current.name.equals(identifier)) {
                return current.supplier;
            }
            toCheck.addAll(current.children);
        }

        return null;
    }

    /**
     * Create a new {@link Node} of the given type.
     * @param identifier the type of Node to create.
     * @return a new instance of the Node, or null if the supplier is not found.
     */
    public Node create(String identifier) {
        Supplier<Node> supplier = getSupplierFor(identifier);
        if(supplier==null) return null;
        return supplier.get();
    }

    /**
     * Scan all classes in the package for classes that extend {@link Node}.
     * @param packageName the package to scan.
     */
    public void scan(String packageName) {
        // Create a new instance of Reflections
        Reflections reflections = new Reflections(packageName);
        // Get all classes that extend T
        Set<Class<? extends Node>> found = reflections.getSubTypesOf(Node.class);
        // log it
        found.stream().sorted(Comparator.comparing(Class::getName)).forEach((clazz)->{
            logger.info("Found " + clazz.getName());
        });
    }

    public void clear() {
        root.children.clear();
    }
}
