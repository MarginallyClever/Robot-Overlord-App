package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;

/**
 * {@link HingeJoint} is a joint that can rotate around the local Z axis.
 */
public class HingeJoint extends Node {
    private double angle = 0;
    private double minAngle = 0;
    private double maxAngle = 180;

    public HingeJoint() {
        this("HingeJoint");
    }

    public HingeJoint(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Pose.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0, 2));

        NumberFormat format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Double.class);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);

        JFormattedTextField angleField = new JFormattedTextField(formatter);
        angleField.setValue(angle);
        angleField.addPropertyChangeListener("value", (evt) ->{
            angle = ((Number) angleField.getValue()).doubleValue();
        });

        JFormattedTextField maxAngleField = new JFormattedTextField(formatter);
        maxAngleField.setValue(maxAngle);
        maxAngleField.addPropertyChangeListener("value", (evt) ->{
            maxAngle = ((Number) maxAngleField.getValue()).doubleValue();
        });

        JFormattedTextField minAngleField = new JFormattedTextField(formatter);
        minAngleField.setValue(minAngle);
        minAngleField.addPropertyChangeListener("value", (evt) ->{
            minAngle = ((Number) minAngleField.getValue()).doubleValue();
        });

        addLabelAndComponent(pane, "Angle",angleField);
        addLabelAndComponent(pane, "Min",minAngleField);
        addLabelAndComponent(pane, "Max",maxAngleField);

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("angle",angle);
        json.put("minAngle",minAngle);
        json.put("maxAngle",maxAngle);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("angle")) angle = from.getDouble("angle");
        if(from.has("minAngle")) minAngle = from.getDouble("minAngle");
        if(from.has("maxAngle")) maxAngle = from.getDouble("maxAngle");
    }

    public double getAngle() {
        return angle;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public double getMaxAngle() {
        return maxAngle;
    }
}
