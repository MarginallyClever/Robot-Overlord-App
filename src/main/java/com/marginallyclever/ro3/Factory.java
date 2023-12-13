package com.marginallyclever.ro3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A factory that can be used to create objects.  It does not manage the objects it creates.
 * @param <T> The class of object to create.
 */
public class Factory<T> {
    /**
     * A category of objects.  These categories can be nested in a tree.
     * @param <T> The class of object to create.
     */
    public static class Category<T> {
        public String name;
        public List<Category<T>> children = new ArrayList<>();
        public final Supplier<T> supplier;

        public Category(String name,Supplier<T> supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public void add(Category<T> c) {
            children.add(c);
        }
    }

    private final Category<T> root = new Category<>("root",null);

    public Category<T> getRoot() {
        return root;
    }

    public Supplier<T> getSupplierFor(String path) {
        List<Category<T>> toCheck = new ArrayList<>(root.children);
        while(!toCheck.isEmpty()) {
            Category<T> current = toCheck.remove(0);
            if(current.name.equals(path)) {
                return current.supplier;
            }
            toCheck.addAll(current.children);
        }

        return null;
    }

    public T create(String path) {
        Supplier<T> supplier = getSupplierFor(path);
        if(supplier==null) return null;
        return supplier.get();
    }
}
