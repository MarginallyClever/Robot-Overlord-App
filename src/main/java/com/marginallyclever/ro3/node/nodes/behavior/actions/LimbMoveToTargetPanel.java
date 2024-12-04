package com.marginallyclever.ro3.node.nodes.behavior.actions;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.nodeselector.NodeSelector;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.*;
import java.awt.*;

public class LimbMoveToTargetPanel extends JPanel {
    public LimbMoveToTargetPanel() {
        this(new LimbMoveToTarget());
    }

    public LimbMoveToTargetPanel(LimbMoveToTarget limbMoveToTarget) {
        super(new GridLayout(0,2));
        this.setName(LimbMoveToTarget.class.getSimpleName());

        // target
        {
            NodeSelector<Pose> selector = new NodeSelector<>(Pose.class, limbMoveToTarget.getTarget());
            selector.addPropertyChangeListener("subject", (evt) -> {
                limbMoveToTarget.setTarget(selector.getSubject());
            });
            PanelHelper.addLabelAndComponent(this, "Target", selector);
        }

        // solver
        {
            NodeSelector<LimbSolver> selector = new NodeSelector<>(LimbSolver.class, limbMoveToTarget.getSolver());
            selector.addPropertyChangeListener("subject", (evt) -> {
                limbMoveToTarget.setSolver(selector.getSubject());
            });
            PanelHelper.addLabelAndComponent(this, "LimbSolver", selector);
        }
    }
}
