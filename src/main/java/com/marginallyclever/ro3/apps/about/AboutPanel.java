package com.marginallyclever.ro3.apps.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class AboutPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(AboutPanel.class);

    public AboutPanel() {
        super(new BorderLayout());
        // Create a JEditorPane
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");  // let the editor pane know this is HTML
        editorPane.setEditable(false); // as a display-only component it's best to make it non-editable
        addHyperlinkListener(editorPane);
        loadAboutFile(editorPane);

        add(editorPane,BorderLayout.CENTER);
    }

    // Add a HyperlinkListener to the editor pane
    private void addHyperlinkListener(JEditorPane editorPane) {
        editorPane.addHyperlinkListener((e)-> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        logger.error("Failed to browse URL {}.",e.getURL(),ex);
                    }
                }
            }
        });
    }

    private void loadAboutFile(JEditorPane editorPane) {
        // Get the URL of the "about.html" resource file
        URL aboutFileURL = getClass().getResource("about.html");
        try {
            // Convert the URL to a Path
            Path aboutFilePath = Paths.get(aboutFileURL.toURI());
            // Read the file content into a String
            String aboutFileContent = Files.readString(aboutFilePath);
            // Set the text of the editor pane to the file content
            editorPane.setText(aboutFileContent);
        } catch (Exception e) {
            editorPane.setText(e.getLocalizedMessage());
        }
    }
}
