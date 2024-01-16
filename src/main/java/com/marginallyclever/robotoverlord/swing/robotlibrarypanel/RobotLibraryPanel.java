package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import com.marginallyclever.ro3.apps.shared.SearchBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of known robots for Robot Overlord and the ability to download and install them.
 */
public class RobotLibraryPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RobotLibraryPanel.class);
    private final JPanel searchBar = new SearchBar();
    private final JPanel repositoriesPanel = new JPanel();
    private final List<RobotLibraryListener> listeners = new ArrayList<>();
    private final List<MultiVersionPropertiesPanel> allPanels = new ArrayList<>();

    public RobotLibraryPanel() {
        super(new BorderLayout());
        setName("RobotLibraryPanel");
        add(searchBar, BorderLayout.NORTH);
        repositoriesPanel.setLayout(new BoxLayout(repositoriesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(repositoriesPanel);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        collectPanels();
        refreshPanels("");

        add(scrollPane, BorderLayout.CENTER);
        searchBar.addPropertyChangeListener("match", evt -> refreshPanels(evt.getNewValue().toString()));
    }

    private void refreshPanels(String match) {
        match = match.toLowerCase();
        List<MultiVersionPropertiesPanel> filteredPanels = new ArrayList<>();
        for(MultiVersionPropertiesPanel panel : allPanels) {
            if (panel.getRobotName().toLowerCase().contains(match)) {
                filteredPanels.add(panel);
            }
        }

        int j=0;
        repositoriesPanel.removeAll();
        for(MultiVersionPropertiesPanel panel : filteredPanels) {
            if (j > 0) repositoriesPanel.add(new JSeparator());

            JPanel containerPanel = new JPanel();
            containerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            containerPanel.setLayout(new BorderLayout());
            containerPanel.add(panel, BorderLayout.CENTER);
            containerPanel.setName("multiVersionPropertiesPanel_" + j);
            containerPanel.setMaximumSize(containerPanel.getPreferredSize());

            repositoriesPanel.add(containerPanel);
            ++j;
        }
        repositoriesPanel.add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }

    private void collectPanels() {
        List<String> repositoryUrls = GithubFetcher.getAllRobotsFile();

        int j=0;
        for (String url : repositoryUrls) {
            MultiVersionPropertiesPanel multiVersionPropertiesPanel = new MultiVersionPropertiesPanel(url);
            if (multiVersionPropertiesPanel.getNumTags() == 0) continue; // Skip repositories with no tags

            multiVersionPropertiesPanel.addRobotLibraryListener(this::fireRobotAdded);

            logger.info("Adding " + url);
            allPanels.add(multiVersionPropertiesPanel);
        }
    }

    public void addRobotLibraryListener(RobotLibraryListener listener) {
        listeners.add(listener);
    }

    private void fireRobotAdded() {
        for(RobotLibraryListener listener : listeners) {
            listener.onRobotAdded();
        }
    }
}