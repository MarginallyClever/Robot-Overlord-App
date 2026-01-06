package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.UUID;

/**
 * Load a scene from a file.
 */
public class ImportScene extends AbstractUndoableEdit {
    private final Logger logger = LoggerFactory.getLogger(ImportScene.class);
    private final File selectedFile;
    private Node created;

    public ImportScene(File selectedFile) {
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
     * Load a scene from a file.
     */
    public void execute() {
        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import scene from {}",selectedFile.getAbsolutePath());

        // do it!
        String newCWD = selectedFile.getParent() + File.separator;
        String oldCWD = PathHelper.getCurrentWorkingDirectory();
        PathHelper.setCurrentWorkingDirectory(newCWD);

        try {
            String content = new String(Files.readAllBytes(Paths.get(selectedFile.getAbsolutePath())));
            witnessProtectionBeforeLoad(content);
            // if the json is bad, this will throw an exception before removing the previous scene.
            var jsonObject = new JSONObject(content);
            // Add the loaded scene to the current scene.
            created = createFromJSON(jsonObject);

            // created will contain "Scene" and "Scene > Environment".  We want neither of these.
            for( Node child : created.getChildren() ) {
                if( !(child.getName().equals("Scene") && child.getName().equals("Environment")) ) {
                    Registry.getScene().addChild(child);
                }
            }

            Registry.getPhysics().deferredAction(created);
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }

        PathHelper.setCurrentWorkingDirectory(oldCWD);
        logger.info("done.");
    }

    /**
     * <p>When importing an asset it might already be loaded.  the two sets would have matching UUIDs,
     * which would confuse the system.  To avoid this, we replace all UUIDs in the content with new ones.
     * {@link Node#witnessProtection()} after the fact changes the UUIDs but not the {@link com.marginallyclever.ro3.node.NodePath}s that refer to them,
     * which breaks all internal links.  To solve this, we do the replacement before loading.</p>
     * <p>Search the content for all reference to "nodeID".  Get the UUID that follows it, and then
     * replace every instance of that UUID with a new one.</p>
     */
    private void witnessProtectionBeforeLoad(String content) {
        logger.debug("Replacing UUIDs in content");
        int count=0;
        int first = 0;
        do {
            // find "nodeID"
            var location = content.indexOf("nodeID", first);
            if( location == -1 ) break; // no more found
            // find the UUID that follows it
            var start = content.indexOf(":", location) + 2;
            var end = content.indexOf("\"", start);
            var oldUUID = content.substring(start, end);
            var newUUID = UUID.randomUUID();
            content = content.replace(oldUUID,newUUID.toString());
            count++;
            first = start;
        } while(true);

        logger.debug("Replaced {} UUIDs in content",count);
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

    public static Node createFromJSON(JSONObject jsonObject) {
        Node loaded = Registry.nodeFactory.create(jsonObject.get("type").toString());
        loaded.fromJSON(jsonObject);
        return loaded;
    }
}