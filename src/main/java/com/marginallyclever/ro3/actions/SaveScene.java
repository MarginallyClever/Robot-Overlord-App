package com.marginallyclever.ro3.actions;

import com.marginallyclever.ro3.RecentFilesMenu;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.robotoverlord.RobotOverlord;
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
    private static final JFileChooser chooser = new JFileChooser();
    private final RecentFilesMenu menu;

    public SaveScene(RecentFilesMenu menu) {
        super("Save Scene");
        this.menu = menu;
        putValue(SHORT_DESCRIPTION,"Save the entire scene to a file.");
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
        saveScene(destinationPath);
        menu.addPath(destinationPath);
    }

    private String askUserForDestinationPath(Component source) {
        chooser.setFileFilter(RobotOverlord.FILE_FILTER);
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

    private void saveScene(String absolutePath) {
        logger.info("Save scene to {}",absolutePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(absolutePath))) {
            writer.write(Registry.getScene().toJSON().toString());
        } catch (IOException e) {
            logger.error("Error saving scene to JSON", e);
        }

        logger.info("done.");
    }
}
