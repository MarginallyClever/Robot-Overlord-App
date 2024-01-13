package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.UndoSystem;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reset the scene to a new empty scene.
 */
public class NewScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(NewScene.class);
    private final SaveScene saveScene;

    public NewScene() {
        this(null);
    }

    public NewScene(SaveScene saveScene) {
        super();
        this.saveScene = saveScene;
        putValue(Action.NAME,"New");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-new-16.png"))));
        putValue(SHORT_DESCRIPTION,"Reset the scene to a new empty scene.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        commitNewScene();
        UndoSystem.reset();
    }

    public void commitNewScene() {
        logger.info("New scene");

        // remove all children of the scene to make sure we're starting fresh.
        Node oldScene = Registry.getScene();
        List<Node> toRemove = new ArrayList<>(oldScene.getChildren());
        for(Node child : toRemove) {
            oldScene.removeChild(child);
        }

        Registry.reset();
        Registry.setScene(new Node("Scene"));

        if(saveScene!=null) saveScene.setEnabled(false);

        logger.info("done.");
    }
}
