package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

public class CrabPanel extends JPanel {
    private final Crab crab;

    // walkStyleNames has to match Crab.WalkStategy.
    private final String [] walkStyleNames = java.util.Arrays.stream(CrabWalkStategy.values())
            .map(Enum::name)
            .toArray(String[]::new);

    public CrabPanel() {
        this(new Crab());
    }

    public CrabPanel(Crab limb) {
        super(new GridBagLayout());

        this.crab = limb;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        JComboBox<String> walkStyle = new JComboBox<>(walkStyleNames);
        setStrategyLabelValue(walkStyle);
        walkStyle.addActionListener(e->{
            crab.setChosenStrategy(CrabWalkStategy.valueOf(Objects.requireNonNull(walkStyle.getSelectedItem()).toString()));
        });
        PanelHelper.addLabelAndComponent(this,"Style",walkStyle);
        gbc.gridy++;

        // add 3x3 grid of buttons.  turn left, forward, turn right, strafe left, stop, strafe right, raise torso, backward, lower torso.
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3));
        buttonPanel.add(createButton("Turn Left", e->crab.turnLeft(1)));
        buttonPanel.add(createButton("Forward", e->crab.forward(1)));
        buttonPanel.add(createButton("Turn Right", e->crab.turnLeft(-5)));
        buttonPanel.add(createButton("Strafe Left", e->crab.strafeRight(-5)));
        buttonPanel.add(createButton("Stop", e->crab.idle()));
        buttonPanel.add(createButton("Strafe Right", e->crab.strafeRight(1)));
        buttonPanel.add(createButton("Raise Torso", e->crab.raiseTorso(1)));
        buttonPanel.add(createButton("Backward", e->crab.forward(-1)));
        buttonPanel.add(createButton("Lower Torso", e->crab.raiseTorso(-1)));

        PanelHelper.addLabelAndComponent(this, "Steering", buttonPanel, gbc);
        gbc.gridy++;
    }

    public static Component createButton(String label, ActionListener actionListener) {
        JButton button = new JButton(label);
        button.addActionListener(actionListener);
        return button;
    }

    private void setStrategyLabelValue(JComboBox<String> walkStyle) {
        String stra = crab.getChosenStrategy().toString();
        int i=0;
        for( var name : walkStyleNames) {
            if (name.equals(stra)) {
                walkStyle.setSelectedIndex(i);
            }
            i++;
        }
    }
}

