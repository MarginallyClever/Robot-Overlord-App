package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.apps.RecentFilesMenu;
import com.marginallyclever.ro3.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Save the entire scene to a file.
 */
public class SaveScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SaveScene.class);
    private final JFileChooser chooser;
    private final RecentFilesMenu menu;

    public SaveScene(RecentFilesMenu menu,JFileChooser chooser) {
        super("Save Scene");
        this.chooser = chooser;
        this.menu = menu;
        putValue(SHORT_DESCRIPTION,"Save to a file.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component source = (Component) e.getSource();
        String destinationPath = askUserForDestinationPath(source);
        if (destinationPath == null) return;  // cancelled
        commitSave(destinationPath);
        menu.addPath(destinationPath);
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
            writer.write(Registry.getScene().toJSON().toString());
        } catch (IOException e) {
            logger.error("Error saving file.  ", e);
        }

        logger.info("done.");
    }
}
