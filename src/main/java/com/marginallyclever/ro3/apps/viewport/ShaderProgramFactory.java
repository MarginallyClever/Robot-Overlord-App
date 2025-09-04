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
     * Releases all shader programs.  This should be called when the OpenGL context is going away.
     * @param gl3 the GL3 context
     */
    public void unloadAll(GL3 gl3) {
        for( Resource<ShaderProgram> sp : shaderPrograms.values() ) {
            sp.resource().unload(gl3);
        }
    }

    public void reset() {
        shaders.values().removeIf(entry -> entry.lifetime()==Lifetime.SCENE);
        shaderPrograms.values().removeIf(entry -> entry.lifetime()==Lifetime.SCENE);
    }
}
