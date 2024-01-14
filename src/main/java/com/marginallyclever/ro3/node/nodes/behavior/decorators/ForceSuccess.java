package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

/**
 * <p>{@link ForceSuccess} is a {@link Decorator} that forces its child to return {@link Status#SUCCESS}.</p>
 */
public class ForceSuccess extends Decorator {
    public ForceSuccess() {
        this("ForceSuccess");
    }

    public ForceSuccess(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        var result = super.tick();
        if(result==Status.RUNNING) return Status.RUNNING;
        return Status.SUCCESS;
    }
}
