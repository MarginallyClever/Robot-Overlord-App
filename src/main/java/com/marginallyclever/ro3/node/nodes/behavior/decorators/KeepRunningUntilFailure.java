package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

/**
 * <p>{@link KeepRunningUntilFailure} is a {@link Decorator} that repeats until its child returns
 * {@link Status#FAILURE}.</p>
 */
public class KeepRunningUntilFailure extends Decorator {
    public KeepRunningUntilFailure() {
        this("KeepRunningUntilFailure");
    }

    public KeepRunningUntilFailure(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        Status result = super.tick();
        if(result != Status.FAILURE) return Status.RUNNING;
        return Status.FAILURE;
    }
}
