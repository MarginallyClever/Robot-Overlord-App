package com.marginallyclever.robotoverlord;

/**
 * Used by {@link com.marginallyclever.robotoverlord.components} to translate between relative
 * and absolute paths as needed.
 *
 * @since 2.6.0
 * @author Dan Royer
 */
public class SerializationContext {
    private final String projectAbsPath;

    public SerializationContext(String projectAbsPath) {
        this.projectAbsPath = projectAbsPath;
    }

    public String getProjectAbsPath() {
        return projectAbsPath;
    }
}
