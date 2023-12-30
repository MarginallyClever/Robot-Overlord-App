package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.RO3Frame;
import com.marginallyclever.ro3.node.Node;
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
        chooser.setFileFilter(RO3Frame.FILE_FILTER);
        
        Component source = (Component) e.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if (chooser.showDialog(parentFrame,"Import") == JFileChooser.APPROVE_OPTION) {
            UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.ImportScene(chooser.getSelectedFile()));
        }
    }
}
