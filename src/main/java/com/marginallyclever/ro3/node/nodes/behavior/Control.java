package com.marginallyclever.ro3.node.nodes.behavior;

/**
 * <p>{@link Control} is a {@link Behavior} that controls the flow of execution.</p>
 */
public abstract class Control extends Behavior {
    public Control() {
        this("Control");
    }

    public Control(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        return Status.FAILURE;
    }
}
