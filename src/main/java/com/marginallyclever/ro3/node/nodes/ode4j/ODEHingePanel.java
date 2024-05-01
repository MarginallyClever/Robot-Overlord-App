package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODEBody;
import com.marginallyclever.ro3.node.nodes.ode4j.odebody.ODEBox;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * A panel for editing an ODEHinge.
 */
public class ODEHingePanel extends JPanel {
    public ODEHingePanel() {
        this(new ODEHinge());
    }

    public ODEHingePanel(ODEHinge hinge) {
        super(new GridLayout(0,2));
        this.setName(ODEHinge.class.getSimpleName());

        addSelector("part A",hinge.getPartA(),hinge::setPartA);
        addSelector("part B",hinge.getPartB(),hinge::setPartB);
        addAction("Torque",hinge);
    }

    private void addSelector(String label, NodePath<ODEBody> originalValue, Consumer<ODEBody> setPartA) {
        NodeSelector<ODEBody> selector = new NodeSelector<>(ODEBody.class,originalValue.getSubject());
        selector.addPropertyChangeListener("subject", (evt) ->setPartA.accept((ODEBody)evt.getNewValue()));
        PanelHelper.addLabelAndComponent(this, label,selector);
    }

    private void addAction(String label,ODEHinge hinge) {
        JPanel panel = new JPanel(new FlowLayout());

        JButton selector = new JButton("+");
        selector.addActionListener((e)-> hinge.addTorque(250000));
        panel.add(selector);

        JButton selector2 = new JButton("-");
        selector2.addActionListener((e)-> hinge.addTorque(-250000));
        panel.add(selector2);

        PanelHelper.addLabelAndComponent(this, label, panel);
    }
}
