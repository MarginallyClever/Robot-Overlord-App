package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

public class HingeJointPanel extends JPanel {

    public HingeJointPanel(HingeJoint hingeJoint) {
        super(new GridLayout(0,2));
        this.setName(HingeJoint.class.getSimpleName());

        NumberFormatter formatter = NumberFormatHelper.getNumberFormatter();

        var angle = hingeJoint.getAngle();
        JFormattedTextField angleField = new JFormattedTextField(formatter);
        angleField.setValue(angle);
        angleField.addPropertyChangeListener("value", (evt) ->{
            hingeJoint.setAngle( ((Number) angleField.getValue()).doubleValue() );
        });

        JFormattedTextField maxAngleField = new JFormattedTextField(formatter);
        maxAngleField.setValue(hingeJoint.getMaxAngle());
        maxAngleField.addPropertyChangeListener("value", (evt) ->{
            hingeJoint.setMaxAngle( ((Number) maxAngleField.getValue()).doubleValue() );
        });

        JFormattedTextField minAngleField = new JFormattedTextField(formatter);
        minAngleField.setValue(hingeJoint.getMinAngle());
        minAngleField.addPropertyChangeListener("value", (evt) ->{
            hingeJoint.setMinAngle( ((Number)minAngleField.getValue()).doubleValue() );
        });

        JFormattedTextField velocityField = new JFormattedTextField(formatter);
        velocityField.setValue(hingeJoint.getVelocity());
        velocityField.addPropertyChangeListener("value", (evt) ->{
            hingeJoint.setVelocity( ((Number)velocityField.getValue()).doubleValue() );
        });

        JFormattedTextField accelerationField = new JFormattedTextField(formatter);
        accelerationField.setValue(hingeJoint.getAngle());
        accelerationField.addPropertyChangeListener("value", (evt) ->{
            hingeJoint.setAcceleration( ((Number)accelerationField.getValue()).doubleValue() );
        });

        NodeSelector<Pose> selector = new NodeSelector<>(Pose.class,hingeJoint.getAxle());
        selector.addPropertyChangeListener("subject", (evt) ->{
            hingeJoint.setAxle(selector.getSubject());
        });

        PanelHelper.addLabelAndComponent(this, "Axle",selector);
        PanelHelper.addLabelAndComponent(this, "Angle",angleField);
        PanelHelper.addLabelAndComponent(this, "Min",minAngleField);
        PanelHelper.addLabelAndComponent(this, "Max",maxAngleField);
        PanelHelper.addLabelAndComponent(this, "Velocity",velocityField);
        PanelHelper.addLabelAndComponent(this, "Acceleration",accelerationField);
    }
}
