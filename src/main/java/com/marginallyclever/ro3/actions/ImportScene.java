package com.marginallyclever.ro3.actions;

import com.marginallyclever.ro3.RecentFilesMenu;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.RobotOverlord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;

/**
 * Load a Scene into the existing Scene.
 */
public class ImportScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(ImportScene.class);
    private static final JFileChooser chooser = new JFileChooser();

    public ImportScene() {
        this("Import Scene");
        putValue(SHORT_DESCRIPTION,"Load a Scene into the existing Scene.");
    }

    public ImportScene(String name) {
        super(name);
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
        if(selectedFile==null) throw new InvalidParameterException("selectedFile is null");

        logger.info("Import scene from {}",selectedFile.getAbsolutePath());

        if( !selectedFile.exists() ) {
            logger.error("File does not exist.");
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            JSONObject json = new JSONObject(content);
            Node loaded = new Node("Scene");
            loaded.fromJSON(json);

            // Add the loaded scene to the current scene.
            Registry.getScene().addChild(loaded);
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }
        logger.info("done.");
    }
}
