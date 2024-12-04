package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for editing an ODEHinge.
 */
public class ODEHingePanel extends JPanel {
    public ODEHingePanel() {
        this(new ODEHinge());
    }

    public ODEHingePanel(ODEHinge hinge) {
        super(new GridBagLayout());
        this.setName(ODEHinge.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;

        addAction(gbc,"Torque",hinge);
        PanelHelper.addLimit(this,gbc,"Angle Max",hinge.getAngleMax(),hinge::setAngleMax,Double.POSITIVE_INFINITY);
        PanelHelper.addLimit(this,gbc,"Angle Min",hinge.getAngleMin(),hinge::setAngleMin,Double.NEGATIVE_INFINITY);
    }

    private void addAction(GridBagConstraints gbc,String label,ODEHinge hinge) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));

        JButton selector = new JButton("+");
        selector.addActionListener((e)-> hinge.addTorque(250000));
        panel.add(selector);

        JButton selector2 = new JButton("-");
        selector2.addActionListener((e)-> hinge.addTorque(-250000));
        panel.add(selector2);

        PanelHelper.addLabelAndComponent(this, label, panel,gbc);
        gbc.gridy++;
    }
}
