package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.NodePanelHelper;

import javax.swing.*;
import java.awt.*;

public class RigidBody3DPanel extends JPanel {
    private final RigidBody3D rigidBody3D;

    public RigidBody3DPanel(RigidBody3D rigidBody3D) {
        super(new GridLayout(0,2));
        this.rigidBody3D = rigidBody3D;
        this.setName(RigidBody3D.class.getSimpleName());

        // mass
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);
        var massField = new JFormattedTextField(formatter);
        massField.setValue(rigidBody3D.getMass());
        massField.addPropertyChangeListener("value", evt -> {
            var value = (Number) massField.getValue();
            rigidBody3D.setMass(value.doubleValue());
        });

        NodePanelHelper.addLabelAndComponent(this,"Mass",massField);
    }
}
