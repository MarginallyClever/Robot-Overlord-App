package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.pluginExplorer.GithubFetcher;
import com.marginallyclever.robotoverlord.swinginterface.pluginExplorer.RobotLibraryPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ShowRobotLibraryPanel extends AbstractAction {
    private static List<String> knownRobots = new ArrayList<>();
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
        if(knownRobots.isEmpty()) {
            knownRobots = GithubFetcher.getAllRobotsFile("MarginallyClever/RobotOverlordArms");
        }
        RobotLibraryPanel panel = new RobotLibraryPanel(knownRobots);
        JFrame frame = new JFrame("Robot Library");
        frame.setLocationRelativeTo(robotOverlord.getMainFrame());
        frame.setPreferredSize(new Dimension(400,600));
        frame.setSize(400,600);
        frame.pack();
        frame.setVisible(true);
    }
}
