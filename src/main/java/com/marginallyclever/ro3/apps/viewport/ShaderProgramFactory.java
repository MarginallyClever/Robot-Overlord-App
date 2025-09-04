package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.Registry;
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
    private final Map<String, Resource<ShaderProgram>> cache = new HashMap<>();

    public ShaderProgram get(Lifetime lifetime, String key, Shader... shaders) {
        return cache.computeIfAbsent(key, _-> new Resource<>(
                        new ShaderProgram(Arrays.stream(shaders).toList()),lifetime)
                ).item();
    }

    /**
     * Releases all shader programs.  This should be called when the OpenGL context is going away.
     * @param gl3 the GL3 context
     */
    public void unloadAll(GL3 gl3) {
        cache.values().forEach(e -> e.item().unload(gl3));
    }

    @Override
    public void removeSceneResources() {
        var list = Registry.toBeUnloaded;
        synchronized (list) {
            // move scene resources to Registry.toBeUnloaded so they can be unloaded in the GL thread.
            cache.values().stream()
                    .filter(r -> r.lifetime() == Lifetime.SCENE)
                    .map(Resource::item)
                    .forEach(list::add);
        }

        cache.values().removeIf(entry -> entry.lifetime()==Lifetime.SCENE);
    }
}
