package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Utility functions for working with paths.
 * @author Dan Royer
 * @since 2.5.0
 */
public class PathUtils {
    private static final Logger logger = LoggerFactory.getLogger(PathUtils.class);

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

    public static String getAppWorkingDirectory() {
        return System.getProperty("user.home") + File.separator + "RobotOverlord";
    }

    public static String getCurrentWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static void setCurrentWorkingDirectory(String dir) {
        System.setProperty("user.dir", dir);
    }

    public static String getAppCacheDirectory() {
        return getAppWorkingDirectory() + File.separator + "Cache";
    }

    public static String getAppPluginsDirectory() {
        return getAppWorkingDirectory() + File.separator + "Plugins";
    }

    public static void goToAppWorkingDirectory() {
        Log.message("Previous directory: "+getCurrentWorkingDirectory());
        // set the current directory to the user's home directory
        String dir = getAppWorkingDirectory();
        setCurrentWorkingDirectory(getAppWorkingDirectory());
        File f = new File(dir);
        if(!f.exists()) f.mkdirs();
        Log.message("Current directory: "+getCurrentWorkingDirectory());
    }
}
