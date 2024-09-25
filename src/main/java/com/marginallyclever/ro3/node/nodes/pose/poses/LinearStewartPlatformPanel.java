package com.marginallyclever.ro3.node.nodes.pose.poses;

import javax.swing.*;
import java.awt.*;

/**
 * GUI for a {@link LinearStewartPlatform}.
 */
public class LinearStewartPlatformPanel extends JPanel {
    private LinearStewartPlatform platform;

    public LinearStewartPlatformPanel() {
        this(new LinearStewartPlatform());
    }

    public LinearStewartPlatformPanel(LinearStewartPlatform platform) {
        super(new GridBagLayout());
        this.platform = platform;
        this.setName(LinearStewartPlatform.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;


    }
}
