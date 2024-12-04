package com.marginallyclever.ro3.apps.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * Save the text area of the {@link EditorPanel} to a file.
 */
public class SaveAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SaveAction.class);
    private final EditorPanel editorPanel;
    private final JFileChooser chooser;

    public SaveAction(EditorPanel editorPanel,JFileChooser chooser) {
        super();
        this.editorPanel = editorPanel;
        this.chooser = chooser;
        putValue(Action.NAME,"Save");
        putValue(SHORT_DESCRIPTION,"Save to a file.");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-save-16.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component source = (Component) e.getSource();
        String destinationPath = askUserForDestinationPath(source);
        if (destinationPath == null) return;  // cancelled
        commitSave(destinationPath);
        logger.debug("Not implemented yet.");
    }

    private String askUserForDestinationPath(Component source) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        int response;
        do {
            if (chooser.showSaveDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
                return null;  // cancelled
            }
            // check before overwriting.
            response = JOptionPane.YES_OPTION;
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile.exists()) {
                response = JOptionPane.showConfirmDialog(parentFrame,
                        "Do you want to replace the existing file?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
            }
            // if the user says no, then loop back to the file chooser.
        } while(response == JOptionPane.NO_OPTION);

        return chooser.getSelectedFile().getAbsolutePath();
    }

    private void commitSave(String absolutePath) {
        logger.info("Save to {}",absolutePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(absolutePath))) {
            writer.write(editorPanel.getText());
        } catch (IOException e) {
            logger.error("Error saving file.  ", e);
        }

        logger.info("done.");
    }
}
