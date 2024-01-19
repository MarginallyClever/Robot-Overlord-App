package com.marginallyclever.ro3.mesh;

import org.junit.jupiter.api.Test;

public class MeshFactoryTest {
    @Test
    public void test() {
        var path = "src/test/resources/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/j0.obj";
        MeshFactory factory = new MeshFactory();
        assert(factory.getPool().getList().isEmpty());
        assert(factory.getAllSourcesForExport().isEmpty());
        assert(factory.canLoad(path));
        var a = factory.load(path);
        assert(!factory.getPool().getList().isEmpty());
        var b = factory.load(path);
        assert(a==b);
        assert(!factory.getAllSourcesForExport().isEmpty());
    }
}
