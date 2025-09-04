package com.marginallyclever.ro3.mesh;

import com.marginallyclever.ro3.factories.Lifetime;
import org.junit.jupiter.api.Test;

public class MeshFactoryTest {
    public static final String path = "src/test/resources/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/j0.obj";

    @Test
    public void test() {
        MeshFactory factory = new MeshFactory();
        assert(factory.getAllResources().isEmpty());
        assert(factory.getAllSourcesForExport().isEmpty());
        assert(factory.canLoad(path));
        var a = factory.get(Lifetime.SCENE,path);
        assert(a.getNumVertices()>0);
        assert(!factory.getAllResources().isEmpty());
        assert(!factory.getResources(Lifetime.SCENE).isEmpty());
        assert(factory.getResources(Lifetime.APPLICATION).isEmpty());
        var b = factory.get(Lifetime.SCENE,path);
        assert(a==b);
        assert(!factory.getAllSourcesForExport().isEmpty());
        factory.removeSceneResources();
        assert(factory.getAllResources().isEmpty());
    }
}
