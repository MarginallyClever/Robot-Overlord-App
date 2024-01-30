package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

import javax.swing.*;
import java.util.Objects;

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

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/node/nodes/behavior/decorators/icons8-cancel-16.png")));
    }
}
