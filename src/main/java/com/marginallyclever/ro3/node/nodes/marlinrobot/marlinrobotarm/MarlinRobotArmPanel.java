package com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * GUI for a {@link MarlinRobotArm}.
 */
public class MarlinRobotArmPanel extends JPanel {
    @SuppressWarnings("unused")
    public MarlinRobotArmPanel() {
        this(new MarlinRobotArm());
    }

    public MarlinRobotArmPanel(MarlinRobotArm marlinRobotArm) {
        super(new GridBagLayout());
        this.setName(MarlinRobotArm.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        PanelHelper.addNodeSelector(this, "Limb", marlinRobotArm.getLimb(), gbc);
        gbc.gridy++;
        PanelHelper.addNodeSelector(this, "Solver", marlinRobotArm.getSolver(), gbc);
        gbc.gridy++;
        PanelHelper.addNodeSelector(this, "Gripper motor", marlinRobotArm.getGripperMotor(), gbc);
    }
}
