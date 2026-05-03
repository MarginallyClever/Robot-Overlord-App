package com.marginallyclever.ro3.node.nodes.marlinrobot;

import com.marginallyclever.ro3.PanelHelper;

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

        addMarlinButton(marlinRobot,gbc,"M114","Get state","Get the current position of the robot.");
        addMarlinButton(marlinRobot,gbc,"G28","Find home","Move all motors to their home position.");
        addMarlinButton(marlinRobot,gbc,"M17","Enable motors","Enable all motors. The robot arm will be able to move and report its position.");
        addMarlinButton(marlinRobot,gbc,"M18","Disable motors","Disable all motors. The robot arm will be free to move, but will not report its position until the motors are re-enabled.");

        gbc.gridy++;
        JButton G0 = new JButton("G0");
        G0.addActionListener(e-> marlinRobot.sendGCode("G0 "+marlinRobot.getMotorsAndFeedrateAsString()));
        PanelHelper.addLabelAndComponent(this, "Go", G0, gbc);
        G0.setToolTipText("Move the robot.");


        gbc.gridx=0;
        gbc.gridwidth=2;
        this.add(getReceiver(),gbc);
        gbc.gridy++;
        this.add(getSender(),gbc);
    }

    private void addMarlinButton(MarlinRobot marlinRobot, GridBagConstraints gbc, String gcode, String label, String tooltip) {
        gbc.gridy++;
        JButton button = new JButton(gcode);
        button.setToolTipText(tooltip);
        button.addActionListener(e-> marlinRobot.sendGCode(gcode));
        PanelHelper.addLabelAndComponent(this, label, button, gbc);
    }


    // Add a text field that will be sent to the robot arm.
    private JPanel getSender() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField input = new JTextField();
        input.addActionListener(e-> marlinRobot.sendGCode(input.getText()) );
        inputPanel.add(input,BorderLayout.CENTER);
        // Add a button to send the text field to the robot arm.
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e-> {
            marlinRobot.sendGCode(input.getText());
        } );

        inputPanel.add(sendButton,BorderLayout.LINE_END);
        return inputPanel;
    }

    // Add a text field to receive messages from the arm.
    private JPanel getReceiver() {
        JPanel outputPanel = new JPanel(new BorderLayout());

        JLabel outputLabel = new JLabel("Output");
        JTextArea output = new JTextArea(10,20);
        output.setEditable(false);
        outputLabel.setLabelFor(output);
        outputLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        outputPanel.add(new JScrollPane(output),BorderLayout.CENTER);
        outputPanel.add(outputLabel,BorderLayout.NORTH);
        output.setMaximumSize(new Dimension(100, output.getPreferredSize().height));
        marlinRobot.addMarlinListener(output::append);

        return outputPanel;
    }
}
