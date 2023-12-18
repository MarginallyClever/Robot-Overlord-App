package com.marginallyclever.ro3.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NewScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(NewScene.class);

    public NewScene() {
        super("New Scene");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        logger.error("New Scene not implemented yet.");
    }
}
