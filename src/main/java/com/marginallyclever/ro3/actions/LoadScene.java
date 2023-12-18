package com.marginallyclever.ro3.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.robotoverlord.RobotOverlord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoadScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(LoadScene.class);
    private static final JFileChooser chooser = new JFileChooser();

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

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            JSONObject json = new JSONObject(content);
            Node loaded = new Node("Scene");
            loaded.fromJSON(json);
            Registry.setScene(loaded);
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }

        logger.info("done.");
    }
}
