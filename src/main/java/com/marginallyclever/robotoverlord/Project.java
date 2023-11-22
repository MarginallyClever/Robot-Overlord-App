package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link Project} is a collection of Entities that have Components that is stored somewhere on disk.
 *
 */
public class Project {
    private static final Logger logger = LoggerFactory.getLogger(Project.class);
    private final int schemaVerison = 1;

    /**
     * The path on disk where the project is stored.  If path is null then the project has not been saved.
     */
    private String path;

    /**
     * The collection of Entities in this Project.
     */
    private final EntityManager entityManager = new EntityManager();

    public Project() {
        super();
        setDefaultPath();
    }

    public Project(String path) {
        super();
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
        logger.debug("Setting path to '{}'", absolutePath);
        File file = new File(absolutePath);
        if(!file.exists()) logger.warn("path '{}' does not exist", absolutePath);
        else if(!file.isDirectory()) logger.warn("path '{}' is not a directory", absolutePath);
        this.path = absolutePath;
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

    public void clear() {
        getEntityManager().clear();
        setDefaultPath();
        //PathHelper.deleteDirectory(new File(getPath()));
        PathHelper.createDirectoryIfNotExists(getPath());
    }

    private void setDefaultPath() {
        setPath(PathHelper.SCENE_PATH);
    }

    /**
     * Attempt to load the file into a new Scene.
     * @param file the file to load
     * @throws IOException if the file cannot be read
     */
    public void load(File file) throws IOException {
        String newPath = file.getAbsolutePath();
        logger.debug("Loading from {}", newPath);

        Path path = Paths.get(newPath);
        String onlyPath = path.getParent().toString();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }

            entityManager.clear();
            SerializationContext context = new SerializationContext(onlyPath);
            loadFromStringWithContext(responseStrBuilder.toString(),context);
        }
        setPath(onlyPath);
    }

    public void save(String absolutePath) throws IOException {
        SerializationContext context = new SerializationContext(absolutePath);

        // try-with-resources will close the file for us.
        try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
            w.write(saveToStringWithContext(context));
        }
    }

    private void loadFromStringWithContext(String string,SerializationContext context) {
        JSONObject json = new JSONObject(string);
        parseJSON(json,context);
    }

    public void parseJSON(JSONObject json,SerializationContext context) {
        if(!json.has("schemaVersion")) {
            // v0
            entityManager.parseJSON(json, context);
        } else {
            int schemaVersion = json.getInt("schemaVersion");
            if(schemaVersion != this.schemaVerison) {
                logger.warn("Schema version mismatch.  Expected {} but got {}",this.schemaVerison,schemaVersion);
            }
            entityManager.parseJSON(json.getJSONObject("entityManager"), context);
        }
    }

    private String saveToStringWithContext(SerializationContext context) {
        return toJSON(context).toString();
    }

    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = new JSONObject();
        json.put("schemaVersion",schemaVerison);
        json.put("entityManager",entityManager.toJSON(context));
        return json;
    }

    /**
     * Bring Entities and assets of another project into this project.
     * @param from the project to fold into this project
     * @throws IOException if the asset files cannot be copied
     */
    public void addProject(Project from) throws IOException {
        addProjectCommon(from,getPath());
    }

    /**
     * Bring Entities and assets of another project into this project.
     * @param from the project to fold into this project
     * @param subPath the subdirectory to copy the assets into
     * @throws IOException if the asset files cannot be copied
     */
    public void addProject(Project from,String subPath) throws IOException {
        String outputPath = getPath()+File.separator+subPath;
        addProjectCommon(from,outputPath);
    }

    /**
     * Bring Entities and assets of another project into this project.
     * @param from the project to fold into this project
     * @param path the path to copy the assets into
     * @throws IOException if the asset files cannot be copied
     */
    private void addProjectCommon(Project from,String path) throws IOException {
        copyDiskAssetsToScenePath(from, path);
        String str = from.saveToStringWithContext(new SerializationContext(from.getPath()));
        Project adjusted = new Project();
        adjusted.loadFromStringWithContext(str,new SerializationContext(path));
        entityManager.addScene(adjusted.entityManager);
    }
}
