package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.urdf.LoadURDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.util.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * <p>Loads a URDF file into the current scene.  See also <a href="https://wiki.ros.org/urdf/XML">URDF XML format</a>.</p>
 *
 * <p>In the URDF specification, the default units of measurement are strictly defined by the <a href="https://en.wikipedia.org/wiki/International_System_of_Units">International System of Units (SI)</a>.
 * This means the expected measurement is meters, where Robot Overlord is in centimeters.</p>
 */
public class ImportURDF extends AbstractUndoableEdit {
    private static final Logger logger = LoggerFactory.getLogger(ImportURDF.class);
    private final File selectedFile;
    private Node created;

    public ImportURDF(File selectedFile) {
        super();
        this.selectedFile = selectedFile;
        execute();
    }

    @Override
    public String getPresentationName() {
        return "Import " + selectedFile.getName();
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    /**
     * Load a URDF file into the current scene.
     */
    public void execute() {
        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import URDF from {}",selectedFile.getAbsolutePath());

        // do it!
        String newCWD = selectedFile.getParent() + File.separator;
        String oldCWD = PathHelper.getCurrentWorkingDirectory();
        PathHelper.setCurrentWorkingDirectory(newCWD);

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile.getAbsolutePath()));
            // Add the loaded scene to the current scene.
            LoadURDF loader = new LoadURDF();
            created = loader.createFromXML(bis);
            Registry.getScene().addChild(created);
            Registry.getPhysics().deferredAction(created);
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }

        PathHelper.setCurrentWorkingDirectory(oldCWD);
        logger.info("done.");
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        Node parent = created.getParent();
        parent.removeChild(created);
        created = null;
    }

}
