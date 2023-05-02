package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Import a scene from a file and add it to the existing scene.
 * @author Dan Royer
 * @since 2.0.0
 */
public class SceneImportAction extends AbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(SceneImportAction.class);

    private final EntityManager entityManager;
    /**
     * The file chooser remembers the last gcodepath.
     */
    private static final JFileChooser fc = new JFileChooser();

    private File preselectedFile = null;

    public SceneImportAction(EntityManager entityManager) {
        super(Translator.get("SceneImportAction.name"));
        this.entityManager = entityManager;
        fc.setFileFilter(RobotOverlord.FILE_FILTER);
        putValue(SMALL_ICON,new UnicodeIcon("🗁"));
        putValue(SHORT_DESCRIPTION, Translator.get("SceneImportAction.shortDescription"));
    }

    public SceneImportAction(EntityManager entityManager, File preselectedFile) {
        this(entityManager);
        putValue(NAME, Translator.get("SceneImportAction.namePreselected",preselectedFile.getName()));
        this.preselectedFile = preselectedFile;
    }

    public static void setLastDirectory(String s) {
        fc.setCurrentDirectory(new File(s));
    }

    public static String getLastDirectory() {
        return fc.getCurrentDirectory().getAbsolutePath();
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Component source = (Component) evt.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        if(preselectedFile !=null) {
            loadFile(preselectedFile,parentFrame);
            return;
        }

        if (fc.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            loadFile(fc.getSelectedFile(),parentFrame);
        }
    }

    public boolean loadFile(File file,JFrame parentFrame) {
        if(!fc.getFileFilter().accept(file)) return false;

        try {
            SceneLoadAction loader = new SceneLoadAction(entityManager);
            EntityManager source = loader.loadNewScene(file,parentFrame);

            updateSceneAssetPaths(source, entityManager);

            UndoSystem.reset();
        } catch(Exception e1) {
            logger.error("failed",e1);
            JOptionPane.showMessageDialog(parentFrame,e1.getLocalizedMessage());
            e1.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * <p>Move assets from source to destination.  When projectA is imported into projectB, any
     * asset with filename <i>projectA/xxxx.yyy</i> should be copied to <i>projectB/projectA/xxxx.yyy</i> and the
     * asset filename in the destination project should be updated to match.</p>
     * <p>When complete the source scene will only contain the root entity.</p>
     *
     * @param source the scene to copy from
     * @param destination the scene to copy to
     * @throws IOException if the copy fails
     */
    private void updateSceneAssetPaths(EntityManager source, EntityManager destination) throws IOException {
        Path path = Path.of(source.getScenePath());

        Path lastPath = path.subpath(path.getNameCount()-1,path.getNameCount());
        String destinationPath = destination.getScenePath() + File.separator + lastPath.toString();

        if(!source.getScenePath().equals(destinationPath)) {
            IOFileFilter dotFileFilter = new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.getName().startsWith(".");
                }

                @Override
                public boolean accept(File dir, String name) {
                    return !name.startsWith(".");
                }
            };

            try {
                FileUtils.copyDirectory(
                        new File(source.getScenePath()),
                        new File(destinationPath),
                        dotFileFilter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        recursivelyUpdatePaths(source,destinationPath);

        // when entities are added to destination they will automatically be removed from source.
        // to prevent concurrent modification exception we have to have a copy of the list.
        List<Entity> entities = new LinkedList<>(source.getRoot().getChildren());
        // now do the move safely.
        for(Entity e : entities) {
            destination.addEntityToParent(e,destination.getRoot());
        }
    }

    /**
     * Update the paths of each asset in the scene.  At this time there are two ({@link MaterialComponent} and
     * {@link MeshFromFile}).  Instead of a lot of work I'm going to just find and update these two classes.
     * @param source
     * @param destinationPath
     */
    private void recursivelyUpdatePaths(EntityManager source, String destinationPath) {
        LinkedList<Entity> list = new LinkedList<>(source.getEntities());
        String originalPath = source.getScenePath();

        while(!list.isEmpty()) {
            Entity e = list.removeFirst();

            MaterialComponent material = e.getComponent(MaterialComponent.class);
            if(material!=null) {
                String oldPath = material.getTextureFilename();
                String newPath = oldPath;
                if(oldPath.startsWith(originalPath)) {
                    newPath = destinationPath + oldPath.substring(originalPath.length());
                }
                material.setTextureFilename(newPath);
            }

            MeshFromFile mesh = e.getComponent(MeshFromFile.class);
            if(mesh!=null) {
                String oldPath = mesh.getFilename();
                String newPath = oldPath;
                if(oldPath.startsWith(originalPath)) {
                    newPath = destinationPath + oldPath.substring(originalPath.length());
                }
                mesh.setFilename(newPath);
            }

            list.addAll(e.getChildren());
        }
    }
}
