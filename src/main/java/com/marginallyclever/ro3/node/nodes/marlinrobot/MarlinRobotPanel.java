package com.marginallyclever.ro3.node.nodes.marlinrobot;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm.MarlinRobotArm;

import javax.swing.*;
import java.awt.*;

/**
 * GUI for a {@link MarlinRobot}.
 */
public class MarlinRobotPanel extends JPanel {
    private final MarlinRobot marlinRobot;

    @SuppressWarnings("unused")
    public MarlinRobotPanel() {
        this(new MarlinRobot());
    }

    public MarlinRobotPanel(MarlinRobot marlinRobot) {
        super(new GridBagLayout());
        this.marlinRobot = marlinRobot;
        this.setName(MarlinRobot.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;

        gbc.gridy++;
        JButton M114 = new JButton("M114");
        M114.addActionListener(e-> marlinRobot.sendGCode("M114"));
        PanelHelper.addLabelAndComponent(this, "Get state", M114,gbc);
        M114.setToolTipText("Get the current position of the robot arm.");

        gbc.gridy++;
        JButton G28 = new JButton("G28");
        G28.addActionListener(e-> marlinRobot.sendGCode("G28"));
        PanelHelper.addLabelAndComponent(this, "Home", G28, gbc);
        G28.setToolTipText("Move all motors to their home position.");

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
        input.addActionListener(e-> marlinRobot.sendGCode(input.getText()) );
        inputPanel.add(input,BorderLayout.CENTER);
        // Add a button to send the text field to the robot arm.
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e-> marlinRobot.sendGCode(input.getText()) );

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
        marlinRobot.addMarlinListener(output::setText);
        return outputPanel;
    }
}
