package com.marginallyclever.ro3.apps.about;

import com.marginallyclever.ro3.apps.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.BufferedInputStream;
import java.util.Objects;

public class AboutPanel extends App {
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
                } else {
                    logger.error("Web browsing is not supported.");
                }
            }
        });
    }

    private void loadAboutFile(JEditorPane editorPane) {
        // Get the URL of the "about.html" resource file
        try(BufferedInputStream stream = new BufferedInputStream(Objects.requireNonNull(getClass().getResourceAsStream(("about.html"))))) {
            // Read the file content into a String
            String aboutFileContent = new String(stream.readAllBytes());
            if(aboutFileContent.isBlank()) throw new Exception("about.html is empty.");
            // Set the text of the editor pane to the file content
            editorPane.setText(aboutFileContent);
        } catch (Exception e) {
            logger.error("Failed to load about.html.",e);
            editorPane.setText(e.getLocalizedMessage());
        }
    }
}
