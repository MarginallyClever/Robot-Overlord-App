package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

/**
 * <p>{@link RunOnce} is a {@link Decorator} that runs its child once and then returns the result of that run.</p>
 */
public class RunOnce extends Decorator {
    private boolean hasRun = false;
    private Status result = Status.FAILURE;

    public RunOnce() {
        this("RunOnce");
    }

    public RunOnce(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        if(!hasRun) {
            result = super.tick();
            if(result!= Status.RUNNING) {
                hasRun = true;
            }
        }
        return result;
    }
}
