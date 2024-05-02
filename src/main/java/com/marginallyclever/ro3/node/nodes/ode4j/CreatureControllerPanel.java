package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.node.Node;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;

/**
 * A panel for controlling an ODE4J creature.
 */
public class CreatureControllerPanel extends JPanel {
    public CreatureControllerPanel() {
        this(new CreatureController());
    }

    public CreatureControllerPanel(CreatureController creatureController) {
        super(new GridLayout(0,2));
        setName(CreatureController.class.getSimpleName());

        List<ODEHinge> toSearch = creatureController.findHinges();
        for(ODEHinge hinge : toSearch) {
            addAction(hinge.getName(),hinge);
        }
    }

    private void addAction(String label,ODEHinge hinge) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));

        JButton selector = new JButton("+");
        selector.addActionListener((e)-> hinge.addTorque(250000));
        panel.add(selector);

        JButton selector2 = new JButton("-");
        selector2.addActionListener((e)-> hinge.addTorque(-250000));
        panel.add(selector2);

        PanelHelper.addLabelAndComponent(this, label, panel);
    }
}
