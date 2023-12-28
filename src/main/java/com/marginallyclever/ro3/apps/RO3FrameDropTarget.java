package com.marginallyclever.ro3.apps;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.actions.ImportScene;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.ro3.mesh.load.MeshFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

/**
 * Allows the user to drop Scenes and Meshes onto the main window.  They will be imported to the existing scene.
 */
public class RO3FrameDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RO3FrameDropTarget.class);

    public RO3FrameDropTarget() {
        super();
        logger.debug("adding drag + drop support...");
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        try {
            Transferable tr = event.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            for (DataFlavor flavor : flavors) {
                logger.debug("Possible flavor: {}", flavor.getMimeType());
                if (flavor.isFlavorJavaFileListType()) {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Object object = tr.getTransferData(flavor);
                    if (object instanceof List<?> list) {
                        if (!list.isEmpty()) {
                            object = list.get(0);
                            if (object instanceof File file) {
                                // drop a mesh
                                if(importMesh(file.getAbsolutePath())) {
                                    event.dropComplete(true);
                                    return;
                                }
                                // drop a scene
                                if(importScene(file)) {
                                    event.dropComplete(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            logger.debug("Drop failed: {}", event);
            event.rejectDrop();
        } catch (Exception e) {
            logger.error("Drop error", e);
            event.rejectDrop();
        }
    }

    private boolean importMesh(String absolutePath) {
        logger.debug("drag importMesh {}",absolutePath);
        if(!MeshFactory.canLoad(absolutePath)) {
            logger.info("can't load file.");
            return false;
        }

        MeshInstance meshInstance = new MeshInstance(getFilenameWithoutExtensionFromPath(absolutePath));
        meshInstance.setMesh(MeshFactory.load(absolutePath));
        Registry.getScene().addChild(meshInstance);
        logger.error("done.");
        return true;
    }

    private boolean importScene(File file) {
        logger.debug("drag importScene {}",file);
        try {
            ImportScene importScene = new ImportScene();
            importScene.commitImport(file);
        } catch (Exception e) {
            logger.error("Error importing scene",e);
            return false;
        }
        return true;
    }

    private String getFilenameWithoutExtensionFromPath(String absolutePath) {
        File f = new File(absolutePath);
        String fullName = f.getName();
        return fullName.substring(0,fullName.lastIndexOf('.'));
    }
}
