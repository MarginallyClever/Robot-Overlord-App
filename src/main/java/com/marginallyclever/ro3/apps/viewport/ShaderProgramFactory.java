package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
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

    public Shader createShader(int type, String[] shaderCode) {
        String key = type+"-"+ Arrays.hashCode(shaderCode);
        if( shaders.containsKey(key) ) return shaders.get(key);
        // not in pool, create it
        Shader shader = new Shader(type,shaderCode,key);
        shaders.put(key,shader);
        return shader;
    }

    public ShaderProgram createShaderProgram(String name, Shader... shaders) {
        if( shaderPrograms.containsKey(name) ) return shaderPrograms.get(name);
        // not in pool, create it
        ShaderProgram sp = new ShaderProgram(Arrays.stream(shaders).toList());
        shaderPrograms.put(name,sp);
        return sp;
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
