package com.marginallyclever.robotoverlord;

import java.io.File;

/**
 * A {@link Project} is a collection of Entities that have Components that is stored somewhere on disk.
 *
 */
public class Project {
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

        logger.debug("Setting scene path to "+absolutePath);
        this.path = absolutePath;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }


}
