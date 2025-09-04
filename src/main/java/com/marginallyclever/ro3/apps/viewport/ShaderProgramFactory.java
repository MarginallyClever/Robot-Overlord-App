package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.factories.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a list of the loaded {@link ShaderProgram} and prevents duplication.  Also provides a centralized place to
 * destroy them when the OpenGL context goes away.
 */
public class ShaderProgramFactory {
    private final Map<String, Resource<Shader>> shaders = new HashMap<>();
    private final Map<String, Resource<ShaderProgram>> shaderPrograms = new HashMap<>();

    public Shader createShader(Lifetime lifetime,int type, String[] shaderCode) {
        String key = type+"-"+ Arrays.hashCode(shaderCode);
        return shaders.computeIfAbsent(key, k-> new Resource<>(
                        new Shader(type,shaderCode,key),lifetime)
                ).resource();
    }

    public ShaderProgram createShaderProgram(Lifetime lifetime, String key, Shader... shaders) {
        return shaderPrograms.computeIfAbsent(key, k-> new Resource<>(
                        new ShaderProgram(Arrays.stream(shaders).toList()),lifetime)
                ).resource();
    }

    /**
     * Deletes (but does not clear) all the shader programs.
     */
    public void unloadAll(GL3 gl3) {
        for( ShaderProgram sp : shaderPrograms.values() ) {
            sp.unload(gl3);
        }
        shaderPrograms.clear();
    }

    public void reset() {
        shaders.clear();
        shaderPrograms.clear();
    }
}
