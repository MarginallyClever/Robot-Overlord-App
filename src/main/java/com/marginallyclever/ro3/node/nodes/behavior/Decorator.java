package com.marginallyclever.ro3.node.nodes.behavior;

import com.marginallyclever.ro3.node.Node;

/**
 * <p>{@link Decorator} is a {@link Behavior} that has exactly one child.  It can be used to modify the behavior of its
 * child.  It is up to the Decorator to decide if, when and how many times the child should be ticked.</p>
 */
public class Decorator extends Behavior {
    public Decorator() {
        this("Decorator");
    }

    public Decorator(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        Behavior child = findFirstChild(Behavior.class);
        if(child==null) return Status.FAILURE;
        return child.tick();
    }
}