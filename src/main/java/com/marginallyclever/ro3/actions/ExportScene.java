package com.marginallyclever.ro3.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Export the scene to a file for sharing on another computer.  This is not the same as saving the scene.
 * Exporting the scene will save the scene and all the assets it uses to a single file.
 */
public class ExportScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SaveScene.class);
    private static final JFileChooser chooser = new JFileChooser();

    public ExportScene() {
        super("Export Scene");
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
            exportScene(absolutePath);
        }
    }

    private void exportScene(String absolutePath) {
        logger.info("Export scene to {}", absolutePath);
        logger.error("Not implemented yet.");
    }
}
