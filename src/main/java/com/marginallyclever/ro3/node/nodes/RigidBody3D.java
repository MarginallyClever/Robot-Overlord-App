package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePanelHelper;
import org.json.JSONObject;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import java.awt.*;
import java.util.List;

/**
 * {@link RigidBody3D} is a {@link Node} that represents a rigid body.
 */
public class RigidBody3D extends Node {
    private double mass = 0;
    private final Matrix3d momentOfIntertia = new Matrix3d();

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

        // mass
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);
        var massField = new JFormattedTextField(formatter);
        massField.setValue(mass);
        NodePanelHelper.addLabelAndComponent(pane,"Mass",massField);

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();
        json.put("mass",mass);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        mass = json.getDouble("mass");
    }
}
