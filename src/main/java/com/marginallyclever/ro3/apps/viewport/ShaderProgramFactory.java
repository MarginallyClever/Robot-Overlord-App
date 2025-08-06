package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a list of the loaded {@link ShaderProgram} and prevents duplication.  Also provides a centralized place to
 * destroy them when the OpenGL context goes away.
 */
public class ShaderProgramFactory {
    private final Map<String,ShaderProgram> shaderPrograms = new HashMap<>();

    public ShaderProgram createShaderProgram(GL3 gl, String type) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Deletes (but does not clear) all the shader programs.
     */
    public void deleteAll() {

    }
}
