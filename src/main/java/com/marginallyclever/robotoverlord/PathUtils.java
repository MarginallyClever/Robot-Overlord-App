package com.marginallyclever.robotoverlord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;

/**
 * Utility functions for working with the local file system.
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

    public static final String APP_BASE =  System.getProperty("user.home") + File.separator + "RobotOverlord";
    public static final String APP_CACHE = APP_BASE + File.separator + "Cache";
    public static final String APP_PLUGINS = APP_BASE + File.separator + "Plugins";

    public static String getCurrentWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static void setCurrentWorkingDirectory(String dir) {
        System.setProperty("user.dir", dir);
    }

    public static void goToAppWorkingDirectory() {
        // set the current directory to the user's home directory
        setCurrentWorkingDirectory(PathUtils.APP_BASE);
        File f = new File(PathUtils.APP_BASE);
        if(!f.exists() && !f.mkdirs()) {
            JOptionPane.showConfirmDialog(
                    null,
                    "Unable to create directory " + PathUtils.APP_BASE,
                    "Error",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
