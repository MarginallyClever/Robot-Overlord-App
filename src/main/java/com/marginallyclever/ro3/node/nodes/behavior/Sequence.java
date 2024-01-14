package com.marginallyclever.ro3.node.nodes.behavior;

/**
 * {@link Sequence} is a {@link Behavior}.  It ticks all its children as long as they return {@link Status#SUCCESS}.
 * If any child returns {@link Status#FAILURE}, the sequence is aborted.
 */
public class Sequence extends Behavior {
    public Sequence() {
        this("Sequence");
    }

    public Sequence(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        for(var child : getChildren()) {
            if(!(child instanceof Behavior b)) continue;
            Status result = b.tick();
            if(result!=Status.SUCCESS) {
                return result;
            }
        }
        return Status.SUCCESS;
    }
}
