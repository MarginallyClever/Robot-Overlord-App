package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.factories.Factory;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.factories.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a list of the loaded {@link ShaderProgram} and prevents duplication.  Also provides a centralized place to
 * destroy them when the OpenGL context goes away.
 */
public class ShaderProgramFactory extends Factory {
    private final Map<String, Resource<Shader>> shaders = new HashMap<>();
    private final Map<String, Resource<ShaderProgram>> shaderPrograms = new HashMap<>();

    public Shader createShader(Lifetime lifetime,int type, String[] shaderCode) {
        String key = type+"-"+ Arrays.hashCode(shaderCode);
        return shaders.computeIfAbsent(key, _ -> new Resource<>(
                        new Shader(type,shaderCode,key),lifetime)
                ).item();
    }

    public ShaderProgram createShaderProgram(Lifetime lifetime, String key, Shader... shaders) {
        return shaderPrograms.computeIfAbsent(key, _-> new Resource<>(
                        new ShaderProgram(Arrays.stream(shaders).toList()),lifetime)
                ).item();
    }

    /**
     * Releases all shader programs.  This should be called when the OpenGL context is going away.
     * @param gl3 the GL3 context
     */
    public void unloadAll(GL3 gl3) {
        shaderPrograms.values().forEach(e -> e.item().unload(gl3));
    }

    @Override
    public void reset() {
        shaders.values().removeIf(entry -> entry.lifetime()==Lifetime.SCENE);
        shaderPrograms.values().removeIf(entry -> entry.lifetime()==Lifetime.SCENE);
    }
}
