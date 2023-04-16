package com.marginallyclever.robotoverlord.swinginterface.pluginExplorer;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class MultiVersionPropertiesPanel extends JPanel {
    private JComboBox<String> tagComboBox;
    private final JPanel propertiesPanel = new JPanel();
    private final JPanel libraryStatus = new JPanel(new BorderLayout());
    private int numTags;

    public MultiVersionPropertiesPanel(String githubRepositoryUrl) {
        setLayout(new BorderLayout());

        List<String> tags = GithubFetcher.fetchTags(githubRepositoryUrl);
        numTags = tags.size();
        if(numTags==0) tags = GithubFetcher.lookForLocalCopy(githubRepositoryUrl);

        JPanel pageEnd = new JPanel(new BorderLayout());
        pageEnd.setName("pageEnd");

        libraryStatus.setName("libraryStatus");

        tagComboBox = new JComboBox<>(tags.toArray(new String[0]));
        tagComboBox.setName("tagComboBox");
        tagComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePropertiesPanel(githubRepositoryUrl, (String) tagComboBox.getSelectedItem());
                updateLibraryStatusPanel(githubRepositoryUrl, (String) tagComboBox.getSelectedItem());
            }
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
                JButton installButton = new JButton("Install");
                installButton.addActionListener(e -> {
                    // Add the install action here
                    installRobotLibrary(githubRepositoryUrl, tag);
                });
                libraryStatus.add(installButton, BorderLayout.CENTER);
            } else {
                // display the path to the library.
                libraryStatus.add(new JLabel(libraryPath), BorderLayout.CENTER);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        libraryStatus.revalidate();
        libraryStatus.repaint();
    }

    private void installRobotLibrary(String githubRepositoryUrl, String tag) {
        try {
            URI uri = new URI(githubRepositoryUrl);
            String[] urlParts = uri.getPath().split("/");
            String owner = urlParts[1];
            String repoName = urlParts[2];

            File destination = new File(GithubFetcher.getLocalPath(owner,repoName,tag));

            if (!destination.exists()) {
                destination.mkdirs();

                try (Git git = Git.cloneRepository()
                        .setURI(githubRepositoryUrl)
                        .setDirectory(destination)
                        .call()) {

                    Ref tagRef = git.tagList().call().stream()
                            .filter(ref -> ref.getName().equals(Constants.R_TAGS + tag))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Tag not found: " + tag));

                    RevWalk revWalk = new RevWalk(git.getRepository());
                    ObjectId objectId = tagRef.getObjectId();
                    RevCommit commit = revWalk.parseCommit(objectId);
                    git.checkout().setName(commit.getName()).call();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

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
        JPanel alignment = new JPanel(new BorderLayout());
        alignment.add(newCenterPanel, BorderLayout.LINE_START);
        alignment.add(new JPanel(), BorderLayout.CENTER);
        propertiesPanel.removeAll();
        propertiesPanel.add(alignment);
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
