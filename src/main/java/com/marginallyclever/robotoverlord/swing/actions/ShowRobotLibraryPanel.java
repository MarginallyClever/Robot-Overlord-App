package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.swing.robotlibrarypanel.RobotLibraryListener;
import com.marginallyclever.robotoverlord.swing.robotlibrarypanel.RobotLibraryPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Show the Robot Library panel.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class ShowRobotLibraryPanel extends AbstractAction {
    private final RobotLibraryListener robotLibraryListener;

    public ShowRobotLibraryPanel(RobotLibraryListener robotLibraryListener) {
        super("Get more robots...");
        this.robotLibraryListener = robotLibraryListener;
    }
    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component source = (Component) e.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        RobotLibraryPanel panel = new RobotLibraryPanel();
        panel.addRobotLibraryListener(robotLibraryListener);
        JFrame frame = new JFrame("Robot Library");
        frame.setContentPane(panel);
        frame.setPreferredSize(new Dimension(450,600));
        frame.setSize(450,600);
        frame.pack();
        frame.setLocationRelativeTo(parentFrame);
        frame.setVisible(true);
    }
}
