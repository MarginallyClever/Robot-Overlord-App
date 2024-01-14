package com.marginallyclever.ro3.node.nodes.behavior;

/**
 * {@link Fallback} is a {@link Behavior}.
 * The purpose is to try different strategies until we find one that "works".
 */
public class Fallback extends Behavior {
    public Fallback() {
        this("Fallback");
    }

    public Fallback(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        for(var child : getChildren()) {
            if(!(child instanceof Behavior b)) continue;
            Status result = b.tick();
            if(result!=Status.FAILURE) {
                return result;
            }
        }
        return Status.FAILURE;
    }
}
