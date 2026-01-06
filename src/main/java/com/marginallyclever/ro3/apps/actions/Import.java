package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.RO3Frame;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.commands.ImportMesh;
import com.marginallyclever.ro3.apps.commands.ImportScene;
import com.marginallyclever.ro3.apps.commands.ImportURDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Load assets from a file source and insert them into the existing Scene.
 */
public class Import extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(Import.class);

    private final JFileChooser chooser;

    public Import() {
        this(null);
    }

    public Import(JFileChooser chooser) {
        super();
        this.chooser = chooser;
        putValue(Action.NAME,"Import...");
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
        if( chooser == null ) throw new InvalidParameterException("file chooser cannot be null");

        Component source = (Component) e.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if (chooser.showDialog(parentFrame,"Import") == JFileChooser.APPROVE_OPTION) {
            commitImport(chooser.getSelectedFile());
        }
    }

    public boolean commitImport(File selectedFile) {
        if( selectedFile == null ) throw new InvalidParameterException("File cannot be null");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist");

        // check extension to guess drop type
        String extension = selectedFile.getAbsolutePath();
        int dotIndex = extension.lastIndexOf('.');
        if (dotIndex < 0) extension = "";
        else extension = extension.substring(dotIndex + 1).toLowerCase();

        if (extension.equals("urdf")) {
            if (importURDF(selectedFile)) return true;
        }
        if(extension.equals("ro")) {
            if(importScene(selectedFile)) return true;
        }
        if(importMesh(selectedFile.getAbsolutePath())) return true;

        // else silently ignore
        return false;
    }

    private boolean importMesh(String absolutePath) {
        logger.debug("drag importMesh {}",absolutePath);
        if(!Registry.meshFactory.canLoad(absolutePath)) {
            logger.error("can't load file.");
            return false;
        }
        try {
            UndoSystem.addEvent(new ImportMesh(new File(absolutePath)));
        } catch (Exception e) {
            logger.error("Error importing mesh",e);
            return false;
        }
        logger.info("done.");
        return true;
    }

    private boolean importScene(File file) {
        logger.debug("drag importScene {}",file);
        try {
            UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.ImportScene(file));
        } catch (Exception e) {
            logger.error("Error importing scene",e);
            return false;
        }
        logger.info("done.");
        return true;
    }

    private boolean importURDF(File file) {
        logger.debug("drag importURDF {}",file);
        try {
            UndoSystem.addEvent(new ImportURDF(file));
        } catch (Exception e) {
            logger.error("Error importing URDF",e);
            return false;
        }
        logger.info("done.");
        return true;
    }
}
