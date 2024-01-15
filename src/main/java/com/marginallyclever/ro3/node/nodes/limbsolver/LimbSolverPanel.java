package com.marginallyclever.ro3.node.nodes.limbsolver;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;

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

        PanelHelper.addNodeSelector(this, "Limb", limbSolver.getLimb(), gbc);

        gbc.gridy++;
        PanelHelper.addNodeSelector(this, "Target", limbSolver.getTarget(), gbc);

        gbc.gridy++;
        addMoveTargetToEndEffector(this,gbc);

        gbc.gridy++;
        var formatter = NumberFormatHelper.getNumberFormatter();
        formatter.setMinimum(0.0);
        JFormattedTextField marginField = new JFormattedTextField(formatter);
        marginField.setValue(limbSolver.getGoalMarginOfError());
        marginField.addPropertyChangeListener("value", evt -> {
            limbSolver.setGoalMarginOfError( ((Number) marginField.getValue()).doubleValue() );
        });
        PanelHelper.addLabelAndComponent(this, "Goal Margin", marginField, gbc);

        gbc.gridy++;
        gbc.gridwidth=2;
        add(createVelocitySlider(),gbc);
    }

    private void addMoveTargetToEndEffector(JPanel pane,GridBagConstraints gbc) {
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
        PanelHelper.addLabelAndComponent(pane, "Target to EE", targetToEE,gbc);
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
