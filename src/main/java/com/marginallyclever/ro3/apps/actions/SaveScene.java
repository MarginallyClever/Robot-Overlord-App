package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.RecentFilesMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;

/**
 * Save the entire scene to a file.
 */
public class SaveScene extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SaveScene.class);
    private String lastLoadedPath;
    private final RecentFilesMenu menu;

    public SaveScene(RecentFilesMenu menu) {
        super();
        this.menu = menu;
        putValue(Action.NAME,"Save");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-save-16.png"))));
        putValue(SHORT_DESCRIPTION,"Save to a file.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component source = (Component) e.getSource();
        String destinationPath = lastLoadedPath;
        try {
            SaveAsScene.commitSave(destinationPath);
        } catch (IOException ioException) {
            logger.error("Error saving file.  ", ioException);
            JOptionPane.showMessageDialog(source,
                    "Error saving file.  " + ioException.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        menu.addPath(destinationPath);
    }

    public void setPath(String absolutePath) {
        lastLoadedPath = absolutePath;
        putValue(Action.SHORT_DESCRIPTION,"Save "+absolutePath);
    }
}
