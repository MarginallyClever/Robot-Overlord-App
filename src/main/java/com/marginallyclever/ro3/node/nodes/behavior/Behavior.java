package com.marginallyclever.ro3.node.nodes.behavior;

import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import java.util.Objects;

/**
 * <p>{@link Behavior} is a {@link com.marginallyclever.ro3.node.Node} that can be used to control
 * the behavior of a robot.
 * See also <a href="https://www.behaviortree.dev/">https://www.behaviortree.dev/</a></p>
 * <p>A signal called "tick" is sent to the root of the tree and propagates through the tree until it reaches a leaf
 * node.  Any Behavior that receives a tick signal executes its callback. This callback must return either</p>
 *<ul>
 *     <li>SUCCESS</li>
 *     <li>FAILURE</li>
 *     <li>RUNNING</li>
 *</ul>
 * <p>RUNNING means that the action needs more time to return a valid result.</p>
 * <p>If a {@link Behavior} has one or more children, it is its responsibility to propagate the tick; each
 * {@link Behavior} type may have different rules about if, when, and how many times children are ticked.</p>
 */
public abstract class Behavior extends Node {
    public enum Status {
        SUCCESS,
        FAILURE,
        RUNNING
    }

    public Behavior() {
        this("Behavior");
    }

    public Behavior(String name) {
        super(name);
    }

    public abstract Status tick();

    /**
     * reset the internal state of the {@link Behavior}.
     */
    public void reset() {
        for(Node n : getChildren()) {
            if(n instanceof Behavior b) b.reset();
        }
    }
}
