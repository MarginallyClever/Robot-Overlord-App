package com.marginallyclever.ro3.node.nodes.limbsolver;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.node.NodePanelHelper;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class LimbSolverPanel extends JPanel {
    private final LimbSolver limbSolver;

    public LimbSolverPanel(LimbSolver limbSolver) {
        super(new GridBagLayout());
        this.limbSolver = limbSolver;
        this.setName(LimbSolver.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;

        gbc.gridwidth=2;
        this.add(createVelocitySlider(),gbc);

        gbc.gridy++;
        gbc.gridwidth=1;
        addMoveTargetToEndEffector(this,gbc);

        gbc.gridy++;
        NodePanelHelper.addNodeSelector(this, "Target", limbSolver.getTarget(), Pose.class, gbc,limbSolver);

        gbc.gridy++;
        gbc.gridwidth=1;
        NodePanelHelper.addNodeSelector(this, "Limb", limbSolver.getLimb(), Limb.class, gbc,limbSolver);

        gbc.gridy++;
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);
        JFormattedTextField marginField = new JFormattedTextField(formatter);
        marginField.setValue(limbSolver.getGoalMarginOfError());
        marginField.addPropertyChangeListener("value", evt -> {
            limbSolver.setGoalMarginOfError( ((Number) marginField.getValue()).doubleValue() );
        });
        NodePanelHelper.addLabelAndComponent(this, "Goal Margin", marginField, gbc);
    }

    private void addMoveTargetToEndEffector(JPanel pane,GridBagConstraints gbc) {
        // move target to end effector
        JButton targetToEE = new JButton(new AbstractAction() {
            {
                putValue(Action.NAME,"Move");
                putValue(Action.SHORT_DESCRIPTION,"Move the Target Pose to the End Effector.");
                putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                        "/com/marginallyclever/ro3/apps/shared/icons8-move-16.png"))));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                limbSolver.moveTargetToEndEffector();
            }
        });
        NodePanelHelper.addLabelAndComponent(pane, "Target to EE", targetToEE,gbc);
    }

    private JComponent createVelocitySlider() {
        JPanel container = new JPanel(new BorderLayout());
        // add a slider to control linear velocity
        JSlider slider = new JSlider(0,20,(int)limbSolver.getLinearVelocity());
        slider.addChangeListener(e-> limbSolver.setLinearVelocity( slider.getValue() ));

        // Make the slider fill the available horizontal space
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, slider.getPreferredSize().height));
        slider.setMinimumSize(new Dimension(50, slider.getPreferredSize().height));

        container.add(new JLabel("Linear Vel"), BorderLayout.LINE_START);
        container.add(slider, BorderLayout.CENTER); // Add slider to the center of the container

        return container;
    }
}
