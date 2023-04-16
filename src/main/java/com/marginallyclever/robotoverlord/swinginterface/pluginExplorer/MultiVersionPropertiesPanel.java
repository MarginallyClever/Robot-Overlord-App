package com.marginallyclever.robotoverlord.swinginterface.pluginExplorer;

import com.marginallyclever.robotoverlord.swinginterface.actions.SceneImportAction;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class MultiVersionPropertiesPanel extends JPanel {
    private final JComboBox<String> tagComboBox;
    private final JPanel propertiesPanel = new JPanel();
    private final JPanel libraryStatus = new JPanel(new BorderLayout());
    private final int numTags;

    public MultiVersionPropertiesPanel(String githubRepositoryUrl) {
        setLayout(new BorderLayout());

        List<String> tags = GithubFetcher.fetchTags(githubRepositoryUrl);
        if(tags.size()==0) tags = GithubFetcher.lookForLocalCopy(githubRepositoryUrl);
        numTags = tags.size();

        JPanel pageEnd = new JPanel(new BorderLayout());
        pageEnd.setName("pageEnd");

        libraryStatus.setName("libraryStatus");

        tagComboBox = new JComboBox<>(tags.toArray(new String[0]));
        tagComboBox.setName("tagComboBox");
        tagComboBox.addActionListener(e -> {
            updatePropertiesPanel(githubRepositoryUrl, (String) tagComboBox.getSelectedItem());
            updateLibraryStatusPanel(githubRepositoryUrl, (String) tagComboBox.getSelectedItem());
        });

        propertiesPanel.setName("propertiesPanel");

        add(propertiesPanel, BorderLayout.CENTER);
        add(pageEnd, BorderLayout.PAGE_END);
        pageEnd.add(tagComboBox, BorderLayout.LINE_START);
        pageEnd.add(libraryStatus,BorderLayout.LINE_END);

        if (!tags.isEmpty()) {
            updatePropertiesPanel(githubRepositoryUrl, tags.get(0));
            updateLibraryStatusPanel(githubRepositoryUrl, tags.get(0));
        }
    }

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
    }

    private void updatePropertiesPanel(String githubRepositoryUrl, String tag) {
        JPanel newCenterPanel;
        try {
            Map<String, String> libraryProperties = GithubFetcher.fetchRobotProperties(githubRepositoryUrl, tag);
            newCenterPanel = new PropertiesPanel(libraryProperties);
        } catch (IOException e) {
            //e.printStackTrace();
            //JOptionPane.showMessageDialog(this, "Error fetching properties for tag " + tag, "Error", JOptionPane.ERROR_MESSAGE);
            newCenterPanel = new JPanel();
            newCenterPanel.add(new JLabel("missing " + tag +" robot.properties."));
        }
        newCenterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        propertiesPanel.removeAll();
        propertiesPanel.add(newCenterPanel);
        propertiesPanel.revalidate();
        propertiesPanel.repaint();
    }

    public int getNumTags() {
        return numTags;
    }

    public static void main(String[] args) {
        MultiVersionPropertiesPanel panel = new MultiVersionPropertiesPanel("https://github.com/MarginallyClever/Sixi-"); // Replace OWNER and REPO with the GitHub repository you want to test
        JFrame frame = new JFrame("MultiVersionPropertiesPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
