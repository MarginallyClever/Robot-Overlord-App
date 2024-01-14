package com.marginallyclever.ro3.node.nodes.behavior;

/**
 * <p>{@link Condition} is a {@link Behavior} that evaluates a condition and returns only SUCCESS or FAILURE.</p>
 */
public abstract class Condition extends Behavior {
    public Condition() {
        this("Condition");
    }

    public Condition(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        return Status.FAILURE;
    }
}
