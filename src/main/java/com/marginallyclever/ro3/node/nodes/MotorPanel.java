package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePanelHelper;

import javax.swing.*;
import java.awt.*;

public class MotorPanel extends JPanel {
    private final Motor motor;

    public MotorPanel(Motor motor) {
        super(new GridLayout(0,2));
        this.motor = motor;
        this.setName(Motor.class.getSimpleName());

        NodeSelector<HingeJoint> selector = new NodeSelector<>(HingeJoint.class,motor.getHinge());
        selector.addPropertyChangeListener("subject", (evt) ->{
            motor.setHinge(selector.getSubject());
        });
        NodePanelHelper.addLabelAndComponent(this, "Hinge", selector);
    }
}
