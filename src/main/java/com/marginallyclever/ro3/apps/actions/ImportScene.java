package com.marginallyclever.ro3.apps.actions;

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
import java.util.Objects;

/**
 * Load a Scene into the existing Scene.
 */
public class ImportScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(ImportScene.class);
    private final JFileChooser chooser;

    public ImportScene() {
        this(null);
    }

    public ImportScene(JFileChooser chooser) {
        super();
        this.chooser = chooser;
        putValue(Action.NAME,"Import Scene");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-import-16.png"))));
        putValue(SHORT_DESCRIPTION,"Load a Scene into the existing Scene.");
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
            commitImport(chooser.getSelectedFile());
        }
    }

    /**
     * Load a scene from a file.
     * @param selectedFile the file to load.
     * @throws InvalidParameterException if the file is null or does not exist.
     */
    public void commitImport(File selectedFile) {
        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import scene from {}",selectedFile.getAbsolutePath());

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            Node loaded = new Node("Scene");
            // Add the loaded scene to the current scene.
            Registry.getScene().addChild(loaded);
            loaded.fromJSON(new JSONObject(content));
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }
        logger.info("done.");
    }
}
