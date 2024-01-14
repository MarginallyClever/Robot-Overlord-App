package com.marginallyclever.ro3.node.nodes.behavior;

/**
 * <p>{@link Action} is a {@link Behavior} that does something and returns a result.</p>
 */
public abstract class Action extends Behavior {
    public Action() {
        this("Action");
    }

    public Action(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        return Status.FAILURE;
    }
}
