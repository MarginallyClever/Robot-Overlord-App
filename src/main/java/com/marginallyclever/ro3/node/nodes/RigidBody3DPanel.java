package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class RigidBody3DPanel extends JPanel {
    public RigidBody3DPanel(RigidBody3D rigidBody3D) {
        super(new GridLayout(0,2));
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

        PanelHelper.addLabelAndComponent(this,"Mass",massField);
    }
}
