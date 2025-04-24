package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.io.File;
import java.security.InvalidParameterException;

/**
 * Load a mesh from a file.
 */
public class ImportMesh extends AbstractUndoableEdit {
    private final Logger logger = LoggerFactory.getLogger(ImportMesh.class);
    private final File selectedFile;
    private final MeshInstance meshInstance;

    public ImportMesh(File selectedFile) {
        super();
        this.selectedFile = selectedFile;

        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import mesh from {}",selectedFile.getAbsolutePath());

        // do it!
        String newCWD = selectedFile.getParent() + File.separator;
        String oldCWD = PathHelper.getCurrentWorkingDirectory();
        System.setProperty("user.dir",newCWD);

        meshInstance = new MeshInstance(getFilenameWithoutExtensionFromPath());
        meshInstance.setMesh(Registry.meshFactory.load(selectedFile.getAbsolutePath()));

        PathHelper.setCurrentWorkingDirectory(oldCWD);
        logger.info("done.");

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
     * Load a scene from a file.
     */
    public void execute() {
        Registry.getScene().addChild(meshInstance);
    }

    private String getFilenameWithoutExtensionFromPath() {
        String fullName = selectedFile.getName();
        return fullName.substring(0,fullName.lastIndexOf('.'));
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        Registry.getScene().removeChild(meshInstance);
    }
}
