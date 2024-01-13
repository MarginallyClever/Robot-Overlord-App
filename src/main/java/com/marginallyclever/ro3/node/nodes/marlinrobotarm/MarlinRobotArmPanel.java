package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Limb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.Objects;

public class MarlinRobotArmPanel extends JPanel {
    private final MarlinRobotArm marlinRobotArm;
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

        NodePanelHelper.addNodeSelector(this, "Limb", marlinRobotArm.getLimb(), Limb.class, gbc,marlinRobotArm);
        gbc.gridy++;
        NodePanelHelper.addNodeSelector(this, "Solver", marlinRobotArm.getSolver(), LimbSolver.class, gbc,marlinRobotArm);
        gbc.gridy++;
        NodePanelHelper.addNodeSelector(this, "Gripper motor", marlinRobotArm.getGripperMotor(), Motor.class, gbc,marlinRobotArm);
        gbc.gridy++;
        JButton M114 = new JButton("M114");
        M114.addActionListener(e-> marlinRobotArm.sendGCode("M114"));
        NodePanelHelper.addLabelAndComponent(this, "Get state", M114,gbc);

        gbc.gridx=0;
        gbc.gridwidth=2;
        this.add(getReceiver(),gbc);
        gbc.gridy++;
        this.add(getSender(),gbc);
        gbc.gridy++;
        this.add(createReportInterval(),gbc);
    }

    private JComponent createReportInterval() {
        var containerPanel = new CollapsiblePanel("Report");
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridBagLayout());

        var label = new JLabel("interval (s)");
        // here i need an input - time interval (positive float, seconds)
        var formatter = NumberFormatHelper.getNumberFormatter();
        var secondsField = new JFormattedTextField(formatter);
        secondsField.setValue(marlinRobotArm.getReportInterval());

        // then a toggle to turn it on and off.
        JToggleButton toggle = new JToggleButton("Start");
        toggle.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/icons8-stopwatch-16.png"))));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setMaximum((int) (marlinRobotArm.getReportInterval() * 1000)); // Assuming interval is in seconds

        Timer timer = new Timer(100, null);
        ActionListener timerAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int value = progressBar.getValue() + 100;
                if (value >= progressBar.getMaximum()) {
                    value = 0;
                    marlinRobotArm.sendGCode("G0"); // Send G0 command when progress bar is full
                }
                progressBar.setValue(value);
            }
        };

        toggle.addActionListener(e -> {
            if (toggle.isSelected()) {
                toggle.setText("Stop");
                timer.addActionListener(timerAction);
                timer.start();
            } else {
                toggle.setText("Start");
                progressBar.setValue(0); // Reset progress bar when toggle is off
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        toggle.addHierarchyListener(e -> {
            if ((HierarchyEvent.SHOWING_CHANGED & e.getChangeFlags()) !=0
                    && !toggle.isShowing()) {
                timer.stop();
                timer.removeActionListener(timerAction);
            }
        });

        // Add components to the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        outerPanel.add(label, gbc);
        gbc.gridx++;
        outerPanel.add(secondsField, gbc);
        gbc.gridy++;
        gbc.gridx=0;
        outerPanel.add(toggle, gbc);
        gbc.gridx++;
        outerPanel.add(progressBar, gbc);
        gbc.gridy++;

        return containerPanel;
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
