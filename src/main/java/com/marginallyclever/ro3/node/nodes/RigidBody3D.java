package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;

/**
 * {@link RigidBody3D} is a {@link Node} that represents a rigid body.
 */
public class RigidBody3D extends Node {
    public RigidBody3D() {
        this("RigidBody3D");
    }

    public RigidBody3D(String name) {
        super(name);
    }
}
