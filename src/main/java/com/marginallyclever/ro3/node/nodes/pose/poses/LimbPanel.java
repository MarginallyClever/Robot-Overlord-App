package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class LimbPanel extends JPanel {
    private final Limb limb;

    public LimbPanel() {
        this(new Limb());
    }

    public LimbPanel(Limb limb) {
        super(new GridBagLayout());
        this.limb = limb;
        this.setName(Limb.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;
        PanelHelper.addNodeSelector(this, "End Effector", limb.getEndEffector(), gbc);

        gbc.gridx=0;
        gbc.gridwidth=2;
        gbc.gridy++;
        this.add(createFKDials(),gbc);

        gbc.gridy++;
        this.add(addMotorPanel(),gbc);
        PanelHelper.addNodeSelector(this, "Target", limb.getTarget(), gbc);
        gbc.gridy++;
        addMoveTargetToEndEffector(this,gbc);
        gbc.gridy++;
        addMoveTargetToFirstSelected(this,gbc);

        gbc.gridy++;
        var formatter = NumberFormatHelper.getNumberFormatterDouble();
        formatter.setMinimum(0.0);
        JFormattedTextField marginField = new JFormattedTextField(formatter);
        marginField.setValue(limb.getGoalMarginOfError());
        marginField.addPropertyChangeListener("value", evt -> {
            limb.setGoalMarginOfError( ((Number) marginField.getValue()).doubleValue() );
        });
        marginField.setToolTipText("The distance between the target and the end effector that is considered 'close enough'.");
        PanelHelper.addLabelAndComponent(this, "Goal Margin", marginField, gbc);

        gbc.gridy++;
        gbc.gridwidth=2;
        add(createVelocitySlider(),gbc);
    }

    private JComponent createFKDials() {
        var containerPanel = new CollapsiblePanel("Forward Kinematics");
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridLayout(0,3));

        int count=0;
        for(int i=0;i<limb.getNumJoints();++i) {
            Motor motor = limb.getJoint(i);
            if(motor==null) continue;
            outerPanel.add(createOneFKDial(motor));
            count++;
        }
        count %= 3;
        if(count != 0) {
            count = 3 - count;
            for (int i = 0; i < count; ++i) {
                outerPanel.add(new JPanel());
            }
        }

        return containerPanel;
    }

    private JPanel createOneFKDial(final Motor motor) {
        JPanel panel = new JPanel(new BorderLayout());
        Dial dial = new Dial();
        dial.addActionListener(e -> {
            limb.setMotorAngle(motor, dial.getValue());
            // update the dial to match the motor's hinge angle (in case of limits)
            dial.setValue(motor.getHinge().getAngle());
        });
        // TODO subscribe to motor.getAxle().getAngle() so the dial matches reality.
        // TODO but also dial.setValue() without triggering an action event, because that's a feedback loop.

        JLabel label = new JLabel(motor.getName());
        label.setLabelFor(dial);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label,BorderLayout.PAGE_START);
        panel.add(dial,BorderLayout.CENTER);
        dial.setPreferredSize(new Dimension(80,80));
        if(motor.hasHinge()) {
            dial.setValue(motor.getHinge().getAngle());
        }
        return panel;
    }

    private JComponent addMotorPanel() {
        var containerPanel = new CollapsiblePanel("Motors");
        containerPanel.setCollapsed(true);
        var outerPanel = containerPanel.getContentPane();
        outerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        // add a selector for each motor
        var motorSelector = new NodeSelector[Limb.MAX_JOINTS];
        for(int i=0;i<Limb.MAX_JOINTS;++i) {
            motorSelector[i] = new NodeSelector<>(Motor.class, limb.getJoint(i));
            int j = i;
            motorSelector[i].addPropertyChangeListener("subject",(e)-> {
                limb.setJoint(j,(Motor)e.getNewValue());
            });
            PanelHelper.addLabelAndComponent(outerPanel, "Motor "+i, motorSelector[i],gbc);
        }
        return containerPanel;
    }

    private void addMoveTargetToFirstSelected(LimbPanel limbPanel, GridBagConstraints gbc) {
        JButton targetToFirstSelected = new JButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Move");
                putValue(Action.SHORT_DESCRIPTION,"Move the Target Pose to the first selected Pose.");
                putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                        "/com/marginallyclever/ro3/apps/shared/icons8-move-16.png"))));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Node node : Registry.selection.getList()) {
                    if(node instanceof Pose pose) {
                        limb.getTarget().getSubject().setWorld(pose.getWorld());
                        break;
                    }
                }
            }
        });
        PanelHelper.addLabelAndComponent(limbPanel, "Target to First Selected", targetToFirstSelected,gbc);
    }

    private void addMoveTargetToEndEffector(JPanel pane,GridBagConstraints gbc) {
        JButton targetToEE = new JButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Freeze!");
                putValue(Action.SHORT_DESCRIPTION,"Move the Target Pose to the End Effector.");
                putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                        "/com/marginallyclever/ro3/apps/shared/icons8-snowflake-16.png"))));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                limb.moveTargetToEndEffector();
            }
        });
        PanelHelper.addLabelAndComponent(pane, "Target to EE", targetToEE,gbc);
    }

    private JComponent createVelocitySlider() {
        JPanel container = new JPanel(new BorderLayout());
        // add a slider to control linear velocity
        JSlider slider = new JSlider(0,20,(int)limb.getLinearVelocity());
        slider.addChangeListener(e-> limb.setLinearVelocity( slider.getValue() ));

        // Make the slider fill the available horizontal space
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, slider.getPreferredSize().height));
        slider.setMinimumSize(new Dimension(50, slider.getPreferredSize().height));
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);

        container.add(new JLabel("Linear Vel"), BorderLayout.LINE_START);
        container.add(slider, BorderLayout.CENTER); // Add slider to the center of the container

        return container;
    }
}
