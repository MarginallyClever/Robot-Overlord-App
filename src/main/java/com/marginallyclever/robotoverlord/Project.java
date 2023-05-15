package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.entityManager.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        setDefaultPath();
    }

    public Project(String path) {
        this();
        setPath(path);
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
        if(!file.exists()) logger.warn("does not exist: "+absolutePath);
        if(!file.isDirectory()) logger.warn("Not a directory: "+absolutePath);
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

        String message = Translator.get("Scene.AssetPathNotInScenePathWarning",unCheckedAssetFilename,getPath());
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
     * @param newPath the new path to use
     */
    public void updateAllComponentWithDiskAsset(String newPath) {
        if(path.equals(newPath)) return;

        LinkedList<Entity> list = new LinkedList<>(getEntityManager().getEntities());
        while(!list.isEmpty()) {
            Entity e = list.removeFirst();
            list.addAll(e.getChildren());

            for(com.marginallyclever.robotoverlord.Component component : e.getComponents()) {
                if(component instanceof ComponentWithDiskAsset) {
                    ((ComponentWithDiskAsset)component).adjustPath(path,newPath);
                }
            }
        }
    }

    public void clear() {
        getEntityManager().clear();
        setDefaultPath();
        PathUtils.deleteDirectory(new File(getPath()));
        PathUtils.createDirectoryIfNotExists(getPath());
    }

    private void setDefaultPath() {
        setPath(PathUtils.SCENE_PATH);
    }

    /**
     * Attempt to load the file into a new Scene.
     * @param file the file to load
     * @throws IOException if the file cannot be read
     */
    public void load(File file) throws IOException {
        String newPath = file.getAbsolutePath();
        logger.debug("Loading from {}", newPath);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }

            entityManager.clear();
            entityManager.parseJSON(new JSONObject(responseStrBuilder.toString()));
        }
        Path path = Paths.get(newPath);
        String onlyPath = path.getParent().toString();
        updateAllComponentWithDiskAsset(onlyPath);
        setPath(onlyPath);
    }

    public void save(String absolutePath) throws IOException {
        updateAllComponentWithDiskAsset("");

        // try-with-resources will close the file for us.
        try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
            w.write(entityManager.toJSON().toString());
        }
    }

    /**
     * Bring Entities and assets of another project into this project.
     * @param from the project to fold into this project
     * @throws IOException if the asset files cannot be copied
     */
    public void addProject(Project from) throws IOException {
        this.copyDiskAssetsToScenePath(from, getPath());
        from.updateAllComponentWithDiskAsset(getPath());
        this.entityManager.addScene(from.entityManager);
    }

    /**
     * Bring Entities and assets of another project into this project.
     * @param from the project to fold into this project
     * @param subPath the subdirectory to copy the assets into
     * @throws IOException if the asset files cannot be copied
     */
    public void addProject(Project from,String subPath) throws IOException {
        String outputPath = getPath()+File.separator+subPath;
        this.copyDiskAssetsToScenePath(from, outputPath);
        from.updateAllComponentWithDiskAsset(outputPath);
        this.entityManager.addScene(from.entityManager);
    }

    public List<String> getAllAssets() {
        List<String> result = new LinkedList<>();
        List<Entity> entityList = new LinkedList<>(entityManager.getEntities());
        while(!entityList.isEmpty()) {
            Entity e = entityList.remove(0);
            entityList.addAll(e.getChildren());
            for(Component component : e.getComponents()) {
                if (component instanceof ComponentWithDiskAsset) {
                    ComponentWithDiskAsset componentWithDiskAsset = (ComponentWithDiskAsset) component;
                    result.addAll( componentWithDiskAsset.getAssetPaths() );
                }
            }
        }
        return result;
    }

    public List<String> getAllAssetsNotInProject() {
        List<String> result = getAllAssets();
        List<String> filtered = new ArrayList<>();
        for(String path : result) {
            if (!new File(path).exists()) {
                filtered.add(path);
            }
        }
        return filtered;
    }
}
