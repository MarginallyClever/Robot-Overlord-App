package com.marginallyclever.ro3.node.nodes.neuralnetwork.leglimbic;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class LegLimbicPanel extends JPanel {
    public LegLimbicPanel() {
        this(new LegLimbic());
    }

    public LegLimbicPanel(LegLimbic legLimbic) {
        super(new GridLayout(0,2));
        setName("Leg Limbic");
    }
}
