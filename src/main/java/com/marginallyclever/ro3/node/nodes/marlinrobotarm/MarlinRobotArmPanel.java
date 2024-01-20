package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class MarlinRobotArmPanel extends JPanel {
    private final MarlinRobotArm marlinRobotArm;

    public MarlinRobotArmPanel() {
        this(new MarlinRobotArm());
    }

    public MarlinRobotArmPanel(MarlinRobotArm marlinRobotArm) {
        super(new GridBagLayout());
        this.marlinRobotArm = marlinRobotArm;
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
        gbc.gridy++;
        JButton M114 = new JButton("M114");
        M114.addActionListener(e-> marlinRobotArm.sendGCode("M114"));
        PanelHelper.addLabelAndComponent(this, "Get state", M114,gbc);

        gbc.gridx=0;
        gbc.gridwidth=2;
        this.add(getReceiver(),gbc);
        gbc.gridy++;
        this.add(getSender(),gbc);
    }

    // Add a text field that will be sent to the robot arm.
    private JPanel getSender() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField input = new JTextField();
        input.addActionListener(e-> marlinRobotArm.sendGCode(input.getText()) );
        inputPanel.add(input,BorderLayout.CENTER);
        // Add a button to send the text field to the robot arm.
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e-> marlinRobotArm.sendGCode(input.getText()) );

        inputPanel.add(sendButton,BorderLayout.LINE_END);
        return inputPanel;
    }

    // Add a text field to receive messages from the arm.
    private JPanel getReceiver() {
        JPanel outputPanel = new JPanel(new BorderLayout());
        JLabel outputLabel = new JLabel("Output");
        JTextField output = new JTextField();
        output.setEditable(false);
        outputLabel.setLabelFor(output);
        outputLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        outputPanel.add(output,BorderLayout.CENTER);
        outputPanel.add(outputLabel,BorderLayout.LINE_START);
        output.setMaximumSize(new Dimension(100, output.getPreferredSize().height));
        marlinRobotArm.addMarlinListener(output::setText);
        return outputPanel;
    }
}
