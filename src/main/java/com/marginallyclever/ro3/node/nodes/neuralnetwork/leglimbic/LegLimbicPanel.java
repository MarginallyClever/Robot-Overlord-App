package com.marginallyclever.ro3.node.nodes.neuralnetwork.leglimbic;

import javax.swing.*;

public class LegLimbicPanel extends JPanel {
    public LegLimbicPanel() {
        this(new LegLimbic());
    }

    public LegLimbicPanel(LegLimbic legLimbic) {
        super();
        setName("Leg Limbic");
    }
}
