package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.actions.ProjectImportAction;
import com.marginallyclever.robotoverlord.swing.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
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
 * This class handles dropping files onto the main window.
 * @since 2.7.0
 * @author Dan Royer
 */
public class MainWindowDropTarget extends DropTargetAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowDropTarget.class);
    private final JFrame mainFrame;
    private final Project project;

    public MainWindowDropTarget(JFrame mainFrame,Project project) {
        logger.debug("adding drag + drop support...");
        this.mainFrame = mainFrame;
        this.project = project;
    }

    @Override
    public synchronized void drop(DropTargetDropEvent event) {
        try {
            Transferable tr = event.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            for (DataFlavor flavor : flavors) {
                logger.debug("Possible flavor: {}", flavor.getMimeType());
                if (flavor.isFlavorJavaFileListType()) {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    Object object = tr.getTransferData(flavor);
                    if (object instanceof List<?>) {
                        List<?> list = (List<?>) object;
                        if (list.size() > 0) {
                            object = list.get(0);
                            if (object instanceof File) {
                                File file = (File) object;
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

    private boolean importScene(File file) {
        ProjectImportAction action = new ProjectImportAction(project);
        return action.loadFile(file,mainFrame);
    }

    private boolean importMesh(String absolutePath) {
        if(!MeshFactory.canLoad(absolutePath)) return false;

        logger.debug("importing mesh "+absolutePath);
        try {
            // create entity.
            Entity entity = new Entity();
            entity.setName(getFilenameWithoutExtensionFromPath(absolutePath));
            // add shape, which will add pose and material.
            ShapeComponent shape = new MeshFromFile(absolutePath);
            entity.addComponent(shape);
            // move entity to camera orbit point so that it is visible.
            PoseComponent pose = entity.getComponent(PoseComponent.class);
            pose.setPosition(project.getEntityManager().getCamera().getOrbitPoint());

            // add entity to scene.
            UndoSystem.addEvent(new EntityAddEdit(project.getEntityManager(), project.getEntityManager().getRoot(),entity));
        } catch(Exception e) {
            logger.error("Error opening file",e);
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
