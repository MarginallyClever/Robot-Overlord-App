package com.marginallyclever.ro3.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Reset the scene to a new empty scene.
 */
public class NewScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(NewScene.class);

    public NewScene() {
        super("New Scene");
        putValue(SHORT_DESCRIPTION,"Reset the scene to a new empty scene.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        logger.info("New scene");

        // remove all children of the scene to make sure we're starting fresh.
        Node oldScene = Registry.getScene();
        List<Node> children = new ArrayList<>(oldScene.getChildren());
        for(Node child : children) {
            oldScene.removeChild(child);
        }
        // remove the scene and replace it completely.
        Registry.setScene(new Node("Scene"));

        logger.info("done.");
    }
}
