package com.marginallyclever.ro3.node.nodes.dogsolver;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.nodes.HingeJoint;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;

import javax.swing.*;
import java.awt.*;

public class DogSolverPanel extends JPanel {
    public DogSolverPanel(DogSolver dogSolver) {
        super(new GridBagLayout());
        this.setName(DogSolver.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;

        // torso
        PanelHelper.addNodeSelector(this, "Torso", dogSolver.getTorso(), gbc);
        gbc.gridy++;

        // legs
        for(int i=0;i<DogSolver.NUM_LEGS;++i) {
            var leg = dogSolver.getLeg(i);
            PanelHelper.addNodeSelector(this, leg.name, leg.limb, gbc);
            gbc.gridy++;
        }

        // gait style
        gbc.gridx=0;
        String [] gaitStyleNames = new String[GaitStyle.values().length];
        for(int i=0;i<GaitStyle.values().length;++i) {
            gaitStyleNames[i] = GaitStyle.values()[i].name();
        }
        var selectStyle = new JComboBox<>(gaitStyleNames);
        PanelHelper.addLabelAndComponent(this,"Gait style",selectStyle,gbc);
        selectStyle.setSelectedItem(dogSolver.getGaitStyle().name());
        selectStyle.addActionListener(e -> {
            dogSolver.setGaitStyle(GaitStyle.valueOf((String)selectStyle.getSelectedItem()));
        });
        selectStyle.setToolTipText("The style of gait the dog will use.");
        gbc.gridy++;

        // stride height
        var strideHeight = new JSpinner(new SpinnerNumberModel(dogSolver.getStrideHeight(),0.0,20.0,0.01));
        PanelHelper.addLabelAndComponent(this,"Stride height (cm)",strideHeight,gbc);
        strideHeight.addChangeListener(e -> {
            dogSolver.setStrideHeight((double)((JSpinner)e.getSource()).getValue());
        });
        strideHeight.setToolTipText("The height of the dog's stride.  cm.");
        gbc.gridy++;

        // forward speed
        var forwardSpeed = new JSpinner(new SpinnerNumberModel(dogSolver.getForwardSpeed(),0.0,20.0,0.01));
        PanelHelper.addLabelAndComponent(this,"Forward speed (cm/s)",forwardSpeed,gbc);
        forwardSpeed.addChangeListener(e -> {
            dogSolver.setForwardSpeed((double)((JSpinner)e.getSource()).getValue());
        });
        forwardSpeed.setToolTipText("The speed at which the dog can move forward.  cm/s");
        gbc.gridy++;

        // strafe speed
        var strafeSpeed = new JSpinner(new SpinnerNumberModel(dogSolver.getStrafeSpeed(),0.0,20.0,0.01));
        PanelHelper.addLabelAndComponent(this,"Strafe speed (cm/s)",strafeSpeed,gbc);
        strafeSpeed.addChangeListener(e -> {
            dogSolver.setStrafeSpeed((double)((JSpinner)e.getSource()).getValue());
        });
        strafeSpeed.setToolTipText("The speed at which the dog can move sideways.  cm/s");
        gbc.gridy++;

        // turn speed
        var turnSpeed = new JSpinner(new SpinnerNumberModel(dogSolver.getTurnSpeed(),0.0,360.0,1.0));
        PanelHelper.addLabelAndComponent(this,"Turn speed (deg/s)",turnSpeed,gbc);
        turnSpeed.addChangeListener(e -> {
            dogSolver.setTurnSpeed((double)((JSpinner)e.getSource()).getValue());
        });
        turnSpeed.setToolTipText("The speed at which the dog can turn.  degrees/s");
        gbc.gridy++;

        // torso standing height
        var torsoStandingHeight = new JSpinner(new SpinnerNumberModel(dogSolver.getTorsoStandingHeight(),0.0,20.0,0.01));
        PanelHelper.addLabelAndComponent(this,"Torso standing height (cm)",torsoStandingHeight,gbc);
        torsoStandingHeight.addChangeListener(e -> {
            dogSolver.setTorsoStandingHeight((double)((JSpinner)e.getSource()).getValue());
        });
        torsoStandingHeight.setToolTipText("The height of the torso when the dog is standing.  cm.");
        gbc.gridy++;

        // torso lying height
        var torsoLyingHeight = new JSpinner(new SpinnerNumberModel(dogSolver.getTorsoLyingHeight(),0.0,20.0,0.01));
        PanelHelper.addLabelAndComponent(this,"Torso lying height (cm)",torsoLyingHeight,gbc);
        torsoLyingHeight.addChangeListener(e -> {
            dogSolver.setTorsoLyingHeight((double)((JSpinner)e.getSource()).getValue());
        });
        torsoLyingHeight.setToolTipText("The height of the torso when the dog is lying down.  cm.");
    }
}
