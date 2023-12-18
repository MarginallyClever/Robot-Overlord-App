package com.marginallyclever.ro3.node.nodes;

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
public class HingeJoint extends Pose {
    private double angle = 0;

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
            angle = (double)angleField.getValue();
        });
        addLabelAndComponent(pane, "Angle",angleField);

        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("angle",angle);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        angle = from.getDouble("angle");
    }
}
