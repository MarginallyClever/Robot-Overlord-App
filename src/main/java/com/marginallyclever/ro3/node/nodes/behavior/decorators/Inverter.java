package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

/**
 * <p>{@link Inverter} is a {@link Decorator} that inverts the result of its child.</p>
 */
public class Inverter extends Decorator {
    public Inverter() {
        this("Inverter");
    }

    public Inverter(String name) {
        super(name);
    }

    @Override
    public Status tick() {
        var result = super.tick();
        if(result==Status.SUCCESS) return Status.FAILURE;
        if(result==Status.FAILURE) return Status.SUCCESS;
        return result;
    }
}
