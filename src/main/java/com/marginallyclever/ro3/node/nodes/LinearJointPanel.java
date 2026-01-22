package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

public class LinearJointPanel extends JPanel {
    public LinearJointPanel() {
        this(new LinearJoint());
    }

    public LinearJointPanel(LinearJoint LinearJoint) {
        super(new GridLayout(0,2));
        this.setName(LinearJoint.class.getSimpleName());

        var angle = LinearJoint.getPosition();
        JFormattedTextField positionField = PanelHelper.addNumberFieldDouble("Position",angle);
        positionField.addPropertyChangeListener("value", (evt) ->{
            LinearJoint.setPosition( ((Number) positionField.getValue()).doubleValue() );
        });

        JFormattedTextField maxPositionField = PanelHelper.addNumberFieldDouble("Maximum",LinearJoint.getMaxPosition());
        maxPositionField.addPropertyChangeListener("value", (evt) ->{
            LinearJoint.setMaxPosition( ((Number) maxPositionField.getValue()).doubleValue() );
        });

        JFormattedTextField minPositionField = PanelHelper.addNumberFieldDouble("Minimum",LinearJoint.getMinPosition());
        minPositionField.addPropertyChangeListener("value", (evt) ->{
            LinearJoint.setMinPosition( ((Number)minPositionField.getValue()).doubleValue() );
        });

        JFormattedTextField velocityField = PanelHelper.addNumberFieldDouble("Velocity",LinearJoint.getVelocity());
        velocityField.addPropertyChangeListener("value", (evt) ->{
            LinearJoint.setVelocity( ((Number)velocityField.getValue()).doubleValue() );
        });

        JFormattedTextField accelerationField = PanelHelper.addNumberFieldDouble("Acceleration",LinearJoint.getPosition());
        accelerationField.addPropertyChangeListener("value", (evt) ->{
            LinearJoint.setAcceleration( ((Number)accelerationField.getValue()).doubleValue() );
        });

        NodeSelector<Pose> selector = new NodeSelector<>(Pose.class,LinearJoint.getCar());
        selector.addPropertyChangeListener("subject", (evt) ->{
            LinearJoint.setCar(selector.getSubject());
        });

        PanelHelper.addLabelAndComponent(this, "Car",selector);
        PanelHelper.addLabelAndComponent(this, "Position",positionField);
        PanelHelper.addLabelAndComponent(this, "Min",minPositionField);
        PanelHelper.addLabelAndComponent(this, "Max",maxPositionField);
        PanelHelper.addLabelAndComponent(this, "Velocity",velocityField);
        PanelHelper.addLabelAndComponent(this, "Acceleration",accelerationField);
    }
}
