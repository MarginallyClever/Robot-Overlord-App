package com.marginallyclever.ro3.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.RobotOverlord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Load a Scene into the existing Scene.
 */
public class ImportScene extends AbstractAction {
    private static final JFileChooser chooser = new JFileChooser();
    private static final Logger logger = LoggerFactory.getLogger(ImportScene.class);

    public ImportScene() {
        super("Import Scene");
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
            loadIntoScene(chooser.getSelectedFile());
        }
    }

    private void loadIntoScene(File selectedFile) {
        logger.info("Import scene from {}",selectedFile.getAbsolutePath());

        // Create an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Read the JSON file and convert it into a Node object
            Node loaded = mapper.readValue(selectedFile, Node.class);

            // option 1: add the loaded scene to the current scene.
            Registry.getScene().addChild(loaded);
            // TODO option 2: copy the loaded scene into the currently selected Nodes of the NodeTreeView?
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }

        logger.error("Import scene not implemented yet.");
    }
}
