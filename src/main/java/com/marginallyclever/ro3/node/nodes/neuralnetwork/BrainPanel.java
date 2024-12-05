package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.apps.neuralnetworkview.BrainView;

import javax.swing.*;

/**
 * {@link BrainPanel} controls settings for a {@link Brain}.  This is different from a
 * {@link BrainView} which illustrates the {@link Brain}'s
 * contents.
 */
public class BrainPanel extends JPanel {
    public BrainPanel() {
        this(new Brain());
    }

    public BrainPanel(Brain brain) {
        super();
        setName("Brain");
    }
}
