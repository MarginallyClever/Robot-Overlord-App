package com.marginallyclever.ro3.node.nodes.odenode;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.odenode.odebody.ODEBody;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * A panel for editing an ODEHinge.
 */
public class ODEBallSocketPanel extends JPanel {
    public ODEBallSocketPanel() {
        this(new ODEBallSocket());
    }

    public ODEBallSocketPanel(ODEBallSocket hinge) {
        super(new GridBagLayout());
        this.setName(ODEHinge.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
    }
}
