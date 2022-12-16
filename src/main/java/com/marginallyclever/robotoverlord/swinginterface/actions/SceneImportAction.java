package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Import a scene from a file and add it to the existing scene.
 * @author Dan Royer
 * @since 2.0.0
 */
public class SceneImportAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SceneImportAction.class);
    private final RobotOverlord ro;

    /**
     * The file chooser remembers the last path.
     */
    private static final JFileChooser fc = new JFileChooser();

    public SceneImportAction(String name, RobotOverlord ro) {
        super(name);
        this.ro=ro;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (fc.showOpenDialog(ro.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                SceneLoadAction loader = new SceneLoadAction("Load Scene",ro);
                Scene source = loader.loadScene(fc.getSelectedFile());
                Scene destination = ro.getScene();

                FileUtils.copyDirectory(
                        new File(source.getScenePath()),
                        new File(destination.getScenePath())
                );

                source.setScenePath(destination.getScenePath());

                // when entities are added to destination they will automatically be removed from source.
                // to prevent concurrent modification exception we have to have a copy of the list.
                List<Entity> entities = new LinkedList<>(source.getEntities());
                // now do the move safely.
                for(Entity e : entities) {
                    destination.addEntity(e);
                }

                UndoSystem.reset();
            } catch(Exception e1) {
                logger.error(e1.getMessage());
                JOptionPane.showMessageDialog(ro.getMainFrame(),e1.getLocalizedMessage());
                e1.printStackTrace();
            }
        }
    }
}
