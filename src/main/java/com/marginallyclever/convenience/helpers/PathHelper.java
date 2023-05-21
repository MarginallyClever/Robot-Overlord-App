package com.marginallyclever.convenience.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;

/**
 * Utility functions for working with the local file system.
 * @author Dan Royer
 * @since 2.5.0
 */
public class PathHelper {
    private static final Logger logger = LoggerFactory.getLogger(PathHelper.class);

    public static final String APP_BASE =  System.getProperty("user.home") + File.separator + "RobotOverlord";
    public static final String APP_CACHE = APP_BASE + File.separator + "Cache";
    public static final String APP_PLUGINS = APP_BASE + File.separator + "Plugins";
    public static final String SCENE_PATH = APP_BASE + File.separator + "Scene";

    /**
     * Get the file extension from a path.
     * @param path The path to get the extension from.
     * @return The extension of the path.
     */
    public static String getExtension(String path) {
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i+1);
        }
        return extension;
    }

    /**
     * Check filename for a valid extension.  If it doesn't have one, add the first extension in the list.
     * @param filename The filename to check.
     * @param extensions A list of valid extensions.
     * @return The filename with a valid extension.
     */
    public static String addExtensionIfNeeded(String filename, String[] extensions) {
        int last = filename.lastIndexOf(".");
        if(last != -1) {
            String end = filename.substring(last + 1).toLowerCase();
            for (String ext : extensions) {
                // has valid extension
                if (end.equals(ext.toLowerCase())) return filename;
            }
        }
        // no matching extension
        return filename + "." + extensions[0];
    }

    public static String getCurrentWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static void setCurrentWorkingDirectory(String dir) {
        System.setProperty("user.dir", dir);
    }

    public static void start() {
        createDirectoryIfNotExists(PathHelper.APP_BASE);
        createDirectoryIfNotExists(PathHelper.APP_CACHE);
        createDirectoryIfNotExists(PathHelper.APP_PLUGINS);
        createDirectoryIfNotExists(PathHelper.SCENE_PATH);
        goToAppWorkingDirectory();
    }

    public static void goToAppWorkingDirectory() {
        // set the current directory to the user's home directory
        setCurrentWorkingDirectory(PathHelper.APP_BASE);
    }

    public static void createDirectoryIfNotExists(String path) {
        File f = new File(path);
        if(!f.exists() && !f.mkdirs()) {
            JOptionPane.showConfirmDialog(
                    null,
                    "Unable to create directory " + PathHelper.APP_BASE,
                    "Error",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Dangerous - recursively deletes directory and all contents!
     * @param directory The directory to delete.
     */
    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
}
