package com.marginallyclever.robotoverlord.swinginterface.robotlibrarypanel;

import com.marginallyclever.robotoverlord.RobotOverlord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of known robots for Robot Overlord and the ability to download and install them.
 * @author Dan Royer
 * @since 2.5.0
 */
public class RobotLibraryPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(RobotLibraryPanel.class);
    private final JPanel searchBar = new JPanel(new BorderLayout());
    private final JTextField match = new JTextField();
    private final JPanel repositoriesPanel = new JPanel();
    private final List<RobotLibraryListener> listeners = new ArrayList<>();
    private final List<MultiVersionPropertiesPanel> allPanels = new ArrayList<>();

    public RobotLibraryPanel() {
        super(new BorderLayout());
        addSearchBar();
        repositoriesPanel.setLayout(new BoxLayout(repositoriesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(repositoriesPanel);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        collectPanels();
        refreshPanels();

        add(scrollPane, BorderLayout.CENTER);

        match.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshPanels();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshPanels();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshPanels();
            }
        });
    }

    private void refreshPanels() {
        List<MultiVersionPropertiesPanel> filteredPanels = new ArrayList<>();
        for(MultiVersionPropertiesPanel panel : allPanels) {
            if (panel.getRobotName().toLowerCase().contains(match.getText().toLowerCase())) {
                filteredPanels.add(panel);
            }
        }

        int j=0;
        repositoriesPanel.removeAll();
        for(MultiVersionPropertiesPanel containerPanel : filteredPanels) {
            if (j > 0) repositoriesPanel.add(new JSeparator());
            repositoriesPanel.add(containerPanel);
            ++j;
        }
        repositoriesPanel.add(Box.createVerticalGlue());
        revalidate();
    }

    private void collectPanels() {
        List<String> repositoryUrls = GithubFetcher.getAllRobotsFile();

        int j=0;
        for (String url : repositoryUrls) {
            MultiVersionPropertiesPanel multiVersionPropertiesPanel = new MultiVersionPropertiesPanel(url);
            if (multiVersionPropertiesPanel.getNumTags() == 0) continue; // Skip repositories with no tags

            multiVersionPropertiesPanel.addRobotLibraryListener(this::fireRobotAdded);


            JPanel containerPanel = new JPanel();
            containerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            containerPanel.setLayout(new BorderLayout());
            containerPanel.add(multiVersionPropertiesPanel, BorderLayout.CENTER);
            containerPanel.setName("multiVersionPropertiesPanel_" + j);
            containerPanel.setMaximumSize(containerPanel.getPreferredSize());
            ++j;

            logger.info("Adding " + url);
            allPanels.add(multiVersionPropertiesPanel);
        }
    }

    private void addSearchBar() {
        searchBar.add(new JLabel(" \uD83D\uDD0E "), BorderLayout.LINE_START);
        searchBar.add(match, BorderLayout.CENTER);
        add(searchBar, BorderLayout.NORTH);
    }

    public void addRobotLibraryListener(RobotLibraryListener listener) {
        listeners.add(listener);
    }

    private void fireRobotAdded() {
        for(RobotLibraryListener listener : listeners) {
            listener.onRobotAdded();
        }
    }

    public static void main(String[] args) {
        RobotLibraryPanel panel = new RobotLibraryPanel();
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