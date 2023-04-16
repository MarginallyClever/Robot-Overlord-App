package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.pluginExplorer.RobotLibraryPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ShowRobotLibraryPanel extends AbstractAction {
    RobotOverlord robotOverlord;

    public ShowRobotLibraryPanel(RobotOverlord robotOverlord) {
        super("Get more robots...");
        this.robotOverlord = robotOverlord;
    }
    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        List<String> names = List.of(
            "https://github.com/MarginallyClever/Sixi-",
            "https://github.com/MarginallyClever/AR4",
            "https://github.com/MarginallyClever/Mecademic-Meca500",
            "https://github.com/MarginallyClever/Arctos",
            "https://github.com/MarginallyClever/K1",
            "https://github.com/MarginallyClever/Mantis",
            "https://github.com/MarginallyClever/Thor"
        );
        RobotLibraryPanel panel = new RobotLibraryPanel(names);
        JFrame frame = new JFrame("Robot Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setPreferredSize(new Dimension(400,600));
        frame.setSize(400,600);
        frame.pack();
        frame.setVisible(true);
    }
}
