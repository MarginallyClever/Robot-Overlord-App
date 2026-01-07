package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;

import java.util.Map;

/**
 * Base class for mechanical joints such as {@link HingeJoint} and {@link LinearJoint}.
 */
public class MechanicalJoint extends Node {
    public MechanicalJoint() {
        this("MechanicalJoint");
    }

    public MechanicalJoint(String name) {
        super(name);
    }
}
