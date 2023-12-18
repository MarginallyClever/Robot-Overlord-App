package com.marginallyclever.ro3.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class LoadScene extends AbstractAction {
    private static final JFileChooser chooser = new JFileChooser();
    private static final Logger logger = LoggerFactory.getLogger(LoadScene.class);

    public LoadScene() {
        super("Load Scene");
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
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            loadAsNewScene(chooser.getSelectedFile());
        }
    }

    private void loadAsNewScene(File selectedFile) {
        logger.info("Load scene from {}",selectedFile.getAbsolutePath());
        logger.error("Load Scene not implemented yet.");
    }
}
