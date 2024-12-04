package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.node.nodes.behavior.Decorator;

import javax.swing.*;
import java.util.Objects;

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

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(
                "/com/marginallyclever/ro3/node/nodes/behavior/decorators/icons8-success-16.png")));
    }
}
