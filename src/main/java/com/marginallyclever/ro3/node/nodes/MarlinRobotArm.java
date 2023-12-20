package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * {@link MarlinRobotArm} converts the state of a robot arm into GCode and back.
 */
public class MarlinRobotArm extends Node {
    public MarlinRobotArm() {
        this("MarlinRobotArm");
    }

    public MarlinRobotArm(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(MarlinRobotArm.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0, 2));

        super.getComponents(list);
    }
}
