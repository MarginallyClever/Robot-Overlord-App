package com.marginallyclever.ro3.node.nodes.pose;

import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.Pose;

import javax.swing.*;
import java.awt.*;

public class LimbPanel extends JPanel {
    private final Limb limb;

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
        NodePanelHelper.addNodeSelector(this, "End Effector", limb.getEndEffector(), Pose.class, gbc);

        gbc.gridx=0;
        gbc.gridwidth=2;
        gbc.gridy++;
        this.add(createFKDials(),gbc);

        gbc.gridy++;
        this.add(addMotorPanel(),gbc);
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
        count = 3-(count%3);
        for(int i=0;i<count;++i) {
            outerPanel.add(new JPanel());
        }

        return containerPanel;
    }

    private JPanel createOneFKDial(final Motor motor) {
        JPanel panel = new JPanel(new BorderLayout());
        Dial dial = new Dial();
        dial.addActionListener(e -> {
            if(motor.hasHinge()) {
                motor.getHinge().setAngle(dial.getValue());
                dial.setValue(motor.getHinge().getAngle());
            }
        });
        // TODO subscribe to motor.getAxle().getAngle(), then dial.setValue() without triggering an action event.

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
            NodePanelHelper.addLabelAndComponent(outerPanel, "Motor "+i, motorSelector[i],gbc);
        }
        return containerPanel;
    }
}
