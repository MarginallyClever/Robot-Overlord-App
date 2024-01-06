package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * {@link RigidBody3D} is a {@link Node} that represents a rigid body.
 */
public class RigidBody3D extends Pose {
    public RigidBody3D() {
        this("RigidBody3D");
    }

    public RigidBody3D(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridLayout(0,2));
        list.add(pane);
        pane.setName(RigidBody3D.class.getSimpleName());

        super.getComponents(list);
    }
}
