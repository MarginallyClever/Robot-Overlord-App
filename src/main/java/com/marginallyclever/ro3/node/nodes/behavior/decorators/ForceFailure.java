package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

/**
 * <p>{@link ForceFailure} is a {@link Decorator} that forces its child to return {@link Status#FAILURE}.</p>
 */
public class ForceFailure extends Decorator {
    public ForceFailure() {
        this("ForceFailure");
    }

    public ForceFailure(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        var result = super.tick();
        if(result==Status.RUNNING) return Status.RUNNING;
        return Status.FAILURE;
    }
}
