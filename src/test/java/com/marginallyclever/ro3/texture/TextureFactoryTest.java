package com.marginallyclever.ro3.texture;

import org.junit.jupiter.api.Test;

public class TextureFactoryTest {
    @Test
    public void test() {
        var path = "src/test/resources/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/SIXI3_BASE.png";
        TextureFactory factory = new TextureFactory();
        assert(factory.getPool().getList().isEmpty());
        assert(factory.getAllSourcesForExport().isEmpty());
        var a = factory.load(path);
        assert(!factory.getPool().getList().isEmpty());
        var b = factory.load(path);
        assert(a==b);
        assert(!factory.getAllSourcesForExport().isEmpty());
    }
}
