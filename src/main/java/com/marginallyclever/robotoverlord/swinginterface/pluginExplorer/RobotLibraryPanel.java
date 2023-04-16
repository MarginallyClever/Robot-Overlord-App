package com.marginallyclever.robotoverlord.swinginterface.pluginExplorer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of known robots for Robot Overlord and the ability to download and install them.
 * @author Dan Royer
 * @since 2.5.0
 */
public class RobotLibraryPanel extends JPanel {

    public RobotLibraryPanel(List<String> repositoryUrls) {
        setLayout(new BorderLayout());

        JPanel repositoriesPanel = new JPanel();
        repositoriesPanel.setLayout(new BoxLayout(repositoriesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(repositoriesPanel);

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

            repositoriesPanel.add(containerPanel);
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        RobotLibraryPanel panel = new RobotLibraryPanel(new ArrayList<>(List.of(
                "https://github.com/MarginallyClever/Sixi-",
                "https://github.com/MarginallyClever/AR4",
                "https://github.com/MarginallyClever/Mecademic-Meca500",
                "https://github.com/MarginallyClever/Arctos",
                "https://github.com/MarginallyClever/K1",
                "https://github.com/MarginallyClever/Mantis",
                //"https://github.com/MarginallyClever/Roci",
                "https://github.com/MarginallyClever/Thor"
        ))); // Replace OWNER and REPO with the GitHub repository you want to test
        JFrame frame = new JFrame("Robot Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setPreferredSize(new Dimension(400,600));
        frame.setSize(400,600);
        frame.pack();
        frame.setVisible(true);
    }
}