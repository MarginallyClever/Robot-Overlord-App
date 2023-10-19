package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Combine many {@link PropertiesPanel}s into one panel. Each {@link PropertiesPanel} is a different version of the
 * same library. The user can select which version to use from a {@link JComboBox}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class MultiVersionPropertiesPanel extends JPanel {
    private final JComboBox<String> tagComboBox;
    private final JPanel propertiesPanelContainer = new JPanel();
    private final JPanel libraryStatus = new JPanel(new BorderLayout());
    private final int numTags;
    private String robotName="missing";

    public MultiVersionPropertiesPanel(String githubRepositoryUrl) {
        setLayout(new BorderLayout());

        List<String> tags = GithubFetcher.fetchTags(githubRepositoryUrl);
        if(tags.size()==0) tags = GithubFetcher.lookForLocallyInstalledTags(githubRepositoryUrl);
        numTags = tags.size();

        JPanel pageEnd = new JPanel(new BorderLayout());
        pageEnd.setName("pageEnd");

        libraryStatus.setName("libraryStatus");

        propertiesPanelContainer.setLayout(new BoxLayout(propertiesPanelContainer, BoxLayout.Y_AXIS));

        tagComboBox = new JComboBox<>(tags.toArray(new String[0]));
        tagComboBox.setName("tagComboBox");
        tagComboBox.addActionListener(e -> {
            updatePropertiesPanel(githubRepositoryUrl, (String) tagComboBox.getSelectedItem());
            updateLibraryStatusPanel(githubRepositoryUrl, (String) tagComboBox.getSelectedItem());
        });

        propertiesPanelContainer.setName("propertiesPanel");

        add(propertiesPanelContainer, BorderLayout.CENTER);
        add(pageEnd, BorderLayout.PAGE_END);
        pageEnd.add(tagComboBox, BorderLayout.LINE_START);
        pageEnd.add(libraryStatus,BorderLayout.LINE_END);

        if (!tags.isEmpty()) {
            updatePropertiesPanel(githubRepositoryUrl, tags.get(0));
            updateLibraryStatusPanel(githubRepositoryUrl, tags.get(0));
        }
    }

    /**
     * Display either the install button or the path to the library.
     * @param githubRepositoryUrl The URL of the repository.
     * @param tag The tag to install.
     */
    private void updateLibraryStatusPanel(String githubRepositoryUrl, String tag) {
        libraryStatus.removeAll();

        try {
            URI uri = new URI(githubRepositoryUrl);
            String[] urlParts = uri.getPath().split("/");
            String owner = urlParts[1];
            String repoName = urlParts[2];
            String libraryPath = GithubFetcher.getLocalPath(owner, repoName, tag);
            if (!new File(libraryPath).exists()) {
                // not installed - display an install button.
                JButton installButton = new JButton("Install");
                installButton.addActionListener(e -> installRobotLibrary(githubRepositoryUrl, tag));
                libraryStatus.add(installButton, BorderLayout.CENTER);
            } else {
                // installed - display the path to the library.
                libraryStatus.add(new JLabel(libraryPath), BorderLayout.CENTER);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        libraryStatus.revalidate();
        libraryStatus.repaint();
    }

    private void installRobotLibrary(String githubRepositoryUrl, String tag) {
        GithubFetcher.installRepository(githubRepositoryUrl, tag);
        updateLibraryStatusPanel(githubRepositoryUrl, tag);
        fireRobotAdded();
    }

    /**
     * Update the properties panel to display the properties for the given tag.
     * @param githubRepositoryUrl The URL of the repository.
     * @param tag The tag to display.
     */
    private void updatePropertiesPanel(String githubRepositoryUrl, String tag) {
        JPanel newCenterPanel;
        try {
            Map<String, String> libraryProperties = GithubFetcher.fetchRobotProperties(githubRepositoryUrl, tag);
            newCenterPanel = new PropertiesPanel(libraryProperties);
            robotName = libraryProperties.get("name");
        } catch (IOException e) {
            //e.printStackTrace();
            //JOptionPane.showMessageDialog(this, "Error fetching properties for tag " + tag, "Error", JOptionPane.ERROR_MESSAGE);
            newCenterPanel = new JPanel();
            newCenterPanel.add(new JLabel("missing " + tag +" robot.properties."));
        }
        newCenterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        propertiesPanelContainer.removeAll();
        propertiesPanelContainer.add(newCenterPanel);
        propertiesPanelContainer.revalidate();
        propertiesPanelContainer.repaint();
    }

    /**
     * Returns the number of tags in the repository.
     * @return The number of tags in the repository.
     */
    public int getNumTags() {
        return numTags;
    }

    List<RobotLibraryListener> listeners = new ArrayList<>();

    public void addRobotLibraryListener(RobotLibraryListener listener) {
        listeners.add(listener);
    }

    private void fireRobotAdded() {
        for(RobotLibraryListener listener : listeners) {
            listener.onRobotAdded();
        }
    }

    public String getRobotName() {
        return robotName;
    }

    /**
     * Launch a test window.
     * @param args Unused.
     */
    public static void main(String[] args) {
        MultiVersionPropertiesPanel panel = new MultiVersionPropertiesPanel("https://github.com/MarginallyClever/Sixi-");
        JFrame frame = new JFrame("MultiVersionPropertiesPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
