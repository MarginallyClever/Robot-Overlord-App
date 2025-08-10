package com.marginallyclever.ro3.node.nodes.crab;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class CrabPanel extends JPanel {
    private final Crab crab;

    // walkStyleNames has to match Crab.WalkStategy.
    private final String [] walkStyleNames = {
            "GO_LIMP",
            "HOME_POSITION",
            "SIT_DOWN",
            "STAND_UP",
            "TAP_TOE_ONE",
            //"WALK_THREE_AT_ONCE",
            //"RIPPLE1",
            //"RIPPLE2",
    };

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
            crab.setChosenStrategy(Crab.WalkStategy.valueOf(Objects.requireNonNull(walkStyle.getSelectedItem()).toString()));
        });
        PanelHelper.addLabelAndComponent(this,"Style",walkStyle);
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

