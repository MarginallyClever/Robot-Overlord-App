package com.marginallyclever.robotoverlord.swinginterface.robotlibrarypanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Displays a list of known robots for Robot Overlord and the ability to download and install them.
 * @author Dan Royer
 * @since 2.5.0
 */
public class RobotLibraryPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RobotLibraryPanel.class);

    public RobotLibraryPanel(List<String> repositoryUrls) {
        setLayout(new BorderLayout());

        JPanel repositoriesPanel = new JPanel();
        repositoriesPanel.setLayout(new BoxLayout(repositoriesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(repositoriesPanel);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        int j=0;
        for (int i = 0; i < repositoryUrls.size(); i++) {
            String url = repositoryUrls.get(i);
            MultiVersionPropertiesPanel multiVersionPropertiesPanel = new MultiVersionPropertiesPanel(url);
            if(multiVersionPropertiesPanel.getNumTags() == 0) continue; // Skip repositories with no tags

            if(j>0) {
                repositoriesPanel.add(new JSeparator());
            }
            JPanel containerPanel = new JPanel();
            containerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            containerPanel.setLayout(new BorderLayout());
            containerPanel.add(multiVersionPropertiesPanel, BorderLayout.CENTER);
            containerPanel.setName("multiVersionPropertiesPanel_" + j);
            ++j;

            logger.info("Adding " + url);
            repositoriesPanel.add(containerPanel);
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        List<String> values = GithubFetcher.getAllRobotsFile("MarginallyClever/RobotOverlordArms");

        RobotLibraryPanel panel = new RobotLibraryPanel(values);
        JFrame frame = new JFrame("Robot Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setPreferredSize(new Dimension(450,600));
        frame.setSize(450,600);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}