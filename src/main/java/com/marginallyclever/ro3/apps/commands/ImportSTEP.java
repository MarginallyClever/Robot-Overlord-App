package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.step.LoadSTEP;
import com.marginallyclever.ro3.urdf.LoadURDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * <p>Loads a STEP file into the current scene.  See also <a href="https://en.wikipedia.org/wiki/ISO_10303-21">STEP format</a>.</p>
 */
public class ImportSTEP extends AbstractUndoableEdit {
    private static final Logger logger = LoggerFactory.getLogger(ImportSTEP.class);
    private final File selectedFile;
    private Node created;

    public ImportSTEP(File selectedFile) {
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
     * Load a STEP file into the current scene.
     */
    public void execute() {
        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import STEP from {}",selectedFile.getAbsolutePath());

        // do it!
        String newCWD = selectedFile.getParent() + File.separator;
        String oldCWD = PathHelper.getCurrentWorkingDirectory();
        PathHelper.setCurrentWorkingDirectory(newCWD);

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile.getAbsolutePath()));
            // Add the loaded scene to the current scene.
            LoadSTEP loader = new LoadSTEP();
            created = loader.createFromStream(bis);
            if(created!=null) {
                Registry.getScene().addChild(created);
                Registry.getPhysics().deferredAction(created);
            }
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
