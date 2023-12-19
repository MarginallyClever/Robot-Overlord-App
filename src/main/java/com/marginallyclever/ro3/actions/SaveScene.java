package com.marginallyclever.ro3.actions;

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

    public SaveScene() {
        super("Save Scene");
        putValue(SHORT_DESCRIPTION,"Save the entire scene to a file.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        chooser.setFileFilter(RobotOverlord.FILE_FILTER);
        Component source = (Component) e.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        if (chooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            // check before overwriting.
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(null,
                        "Do you want to replace the existing file?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            String absolutePath = chooser.getSelectedFile().getAbsolutePath();
            saveScene(absolutePath);
        }
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
