package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.util.PropertiesFileHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * A {@link Project} is a collection of Entities that have Components that is stored somewhere on disk.
 *
 */
public class Project {
    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    /**
     * The path on disk where the project is stored.  If path is null then the project has not been saved.
     */
    private String path;

    /**
     * The collection of Entities in this Project.
     */
    private final EntityManager entityManager;

    public Project() {
        entityManager = new EntityManager();
    }

    public Project(String path) {
        this();
        this.path = path;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public String getPath() {
        return path;
    }

    /**
     * Set the path.  This is the path to the directory containing the scene file.
     * @param absolutePath the absolute path to the scene directory.
     */
    public void setPath(String absolutePath) {
        File file = new File(absolutePath);
        if(!file.exists()) throw new RuntimeException("File does not exist: "+absolutePath);
        if(!file.isDirectory()) throw new RuntimeException("Not a directory: "+absolutePath);
        //if(!entities.isEmpty()) throw new RuntimeException("Cannot change the scene path when entities are present.");

        logger.debug("Setting path to "+absolutePath);
        this.path = absolutePath;
    }

    /**
     * Returns true if unCheckedAssetFilename is in the scene path.
     * @param unCheckedAssetFilename a file that may or may not be within the scene path.
     * @return true if unCheckedAssetFilename is in the scene path.
     */
    public boolean isAssetPathInScenePath(String unCheckedAssetFilename) {
        Path input = Paths.get(unCheckedAssetFilename);
        Path scene = Paths.get(getPath());
        return input.toAbsolutePath().startsWith(scene.toAbsolutePath());
    }

    /**
     * Displays a warning to the user if the asset is not within the scene path.
     * @param unCheckedAssetFilename a file that may or may not be within the scene path.
     */
    public void warnIfAssetPathIsNotInScenePath(String unCheckedAssetFilename) {
        if(isAssetPathInScenePath(unCheckedAssetFilename)) return;

        String message = Translator.get("Scene.AssetPathNotInScenePathWarning");
        message = message.replace("%1", unCheckedAssetFilename);
        message = message.replace("%2", getPath());
        logger.warn("asset "+unCheckedAssetFilename+" not in scene path: "+getPath());

        // try to show a pop-up if we have a display
        if(!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    Translator.get("Scene.AssetPathNotInScenePathWarningTitle"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public String checkForScenePath(String fn) {
        if (!isAssetPathInScenePath(fn)) {
            String fn2 = addScenePath(fn);
            if ((new File(fn2)).exists()) {
                return fn2;
            }
        } else {
            warnIfAssetPathIsNotInScenePath(fn);
        }
        return fn;
    }

    /**
     * Returns the relative path to the asset, or absolute if the asset is not within the scene gcodepath.
     * @param unCheckedAssetFilename a file that may or may not be within the scene gcodepath.
     * @return the relative gcodepath to the asset, or absolute if the asset is not within the scene gcodepath.
     */
    public String removeScenePath(String unCheckedAssetFilename) {
        if(unCheckedAssetFilename==null) return null;

        String scenePathValue = getPath();
        if(unCheckedAssetFilename.startsWith(scenePathValue)) {
            return unCheckedAssetFilename.substring(scenePathValue.length());
        }
        return unCheckedAssetFilename;
    }

    public String addScenePath(String fn) {
        return getPath() + fn;
    }


    public void copyDiskAssetsToScenePath(Project source, String destinationPath) throws IOException {
        if(source.getPath().equals(destinationPath)) return;
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

        FileUtils.copyDirectory(
                new File(source.getPath()),
                new File(destinationPath),
                dotFileFilter);
    }

    /**
     * Update the paths of each asset in the scene.  At this time there are two ({@link MaterialComponent} and
     * {@link MeshFromFile}).  Instead of a lot of work I'm going to just find and update these two classes.
     * @param source the scene to update
     * @param newPath the new path to use
     */
    public void updateAllComponentWithDiskAsset(Project source, String newPath) {
        String originalPath = source.getPath();
        if(originalPath.equals(newPath)) return;

        LinkedList<Entity> list = new LinkedList<>(source.getEntityManager().getEntities());
        while(!list.isEmpty()) {
            Entity e = list.removeFirst();
            list.addAll(e.getChildren());

            for(com.marginallyclever.robotoverlord.Component component : e.getComponents()) {
                if(component instanceof ComponentWithDiskAsset) {
                    ((ComponentWithDiskAsset)component).adjustPath(originalPath,newPath);
                }
            }
        }
    }

    public void clear() {
        getEntityManager().clear();
        setPath("");
    }

    /**
     * Attempt to load the file into a new Scene.
     * @param file the file to load
     * @throws IOException if the file cannot be read
     */
    public void load(File file) throws IOException {
        logger.debug("Loading from {}", file.getAbsolutePath());

        setPath(file.getAbsolutePath());

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }

            String pathName = (Paths.get(file.getAbsolutePath())).getParent().toString();
            entityManager.clear();
            entityManager.parseJSON(new JSONObject(responseStrBuilder.toString()));
        }
    }

    public void save(String absolutePath) throws IOException {
        // try-with-resources will close the file for us.
        try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
            w.write(entityManager.toJSON().toString());
        }
    }

    public void addProject(Project source) throws IOException {
        this.copyDiskAssetsToScenePath(source, getPath());
        this.updateAllComponentWithDiskAsset(source, getPath());
        this.entityManager.addScene(source.entityManager);
    }
}
