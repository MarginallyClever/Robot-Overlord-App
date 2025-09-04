package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.factories.Factory;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.factories.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShaderFactory extends Factory {
    private final Map<String, Resource<Shader>> shaders = new HashMap<>();

    public Shader get(Lifetime lifetime, int type, String[] shaderCode) {
        String typeName = switch(type) {
            case GL3.GL_VERTEX_SHADER -> "VERTEX";
            case GL3.GL_FRAGMENT_SHADER -> "FRAGMENT";
            case GL3.GL_GEOMETRY_SHADER -> "GEOMETRY";
            case GL3.GL_TESS_CONTROL_SHADER -> "TESS_CONTROL";
            case GL3.GL_TESS_EVALUATION_SHADER -> "TESS_EVALUATION";
            case GL3.GL_COMPUTE_SHADER -> "COMPUTE";
            default -> throw new IllegalArgumentException("Invalid shader type: "+type);
        };
        String key = typeName+" "+ Integer.toHexString(Arrays.hashCode(shaderCode));
        return shaders.computeIfAbsent(key, _ -> new Resource<>(
                new Shader(type,shaderCode,key),lifetime)
        ).item();
    }

    /**
     * Releases all shader programs.  This should be called when the OpenGL context is going away.
     * @param gl3 the GL3 context
     */
    public void unloadAll(GL3 gl3) {
        shaders.values().forEach(e -> e.item().unload(gl3));
    }

    @Override
    public void reset() {
        shaders.values().removeIf(entry -> entry.lifetime()==Lifetime.SCENE);
    }
}
