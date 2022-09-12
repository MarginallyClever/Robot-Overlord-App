package com.marginallyclever.robotoverlord.swinginterface.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

/**
 * Use the desktop browser to open a URL.
 * @author Dan Royer
 * @since 2022-03-14
 */
public class BrowseURLAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(BrowseURLAction.class);

    private final String address;

    public BrowseURLAction(String label, String address) {
        super(label);
        this.address = address;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(address));
            }
        } catch (IOException ex) {
            logger.warn("Could not open browser.", ex);
            JOptionPane.showMessageDialog((Component)e.getSource(), "Could not open "+address, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
