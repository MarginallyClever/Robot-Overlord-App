package com.marginallyclever.ro3;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A factory that can be used to create objects.  It does not manage the objects it creates.
 * @param <T> The class of object to create.
 */
public class NodeFactory<T> {
    private static final Logger logger = LoggerFactory.getLogger(NodeFactory.class);
    private final Class<T> type;

    /**
     * A category of objects.  These categories can be nested in a tree.
     * @param <T> The class of object to create.
     */
    public static class Category<T> {
        private final String name;
        private final Supplier<T> supplier;
        private final List<Category<T>> children = new ArrayList<>();
        private Category<T> parent=null;

        public Category(String name,Supplier<T> supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public void add(Category<T> c) {
            children.add(c);
            c.parent = this;
        }

        public Category<T> add(String name,Supplier<T> supplier) {
            Category<T> item = new Category<>(name,supplier);
            add(item);
            return item;
        }

        public String getName() {
            return name;
        }

        public Category<T> getParent() {
            return parent;
        }

        public List<Category<T>> getChildren() {
            return children;
        }

        public Supplier<T> getSupplier() {
            return supplier;
        }
    }

    private final Category<T> root = new Category<>("root",null);

    public NodeFactory(Class<T> type) {
        this.type = type;
        scan();
    }

    public Category<T> getRoot() {
        return root;
    }

    public Supplier<T> getSupplierFor(String path) {
        List<Category<T>> toCheck = new ArrayList<>(root.children);
        while(!toCheck.isEmpty()) {
            Category<T> current = toCheck.remove(0);
            toCheck.addAll(current.children);

            if(current.name.equals(path)) {
                return current.supplier;
            }
        }

        return null;
    }

    public T create(String path) {
        Supplier<T> supplier = getSupplierFor(path);
        if(supplier==null) return null;
        return supplier.get();
    }

    public void scan() {
        // Create a new instance of Reflections
        Reflections reflections = new Reflections("com.marginallyclever.ro3");
        // Get all classes that extend T
        Set<Class<? extends T>> found = reflections.getSubTypesOf(type);
        // Now, classes contains all classes that extend T
        for (Class<? extends T> clazz : found) {
            logger.info("Found " + clazz.getName());
        }
    }

    public void clear() {
        root.children.clear();
    }
}
