package com.marginallyclever.ro3.texture;

import com.marginallyclever.ro3.factories.Lifetime;
import org.junit.jupiter.api.Test;

public class TextureFactoryTest {
    @Test
    public void test() {
        var path = "src/test/resources/com/marginallyclever/ro3/node/nodes/marlinrobotarm/SIXI3_BASE.png";
        TextureFactory factory = new TextureFactory();
        assert(factory.getAllResources().isEmpty());
        assert(factory.getAllResources().isEmpty());
        var a = factory.get(Lifetime.SCENE,path);
        assert(!factory.getAllResources().isEmpty());
        var b = factory.get(Lifetime.SCENE,path);
        assert(a==b);
        assert(!factory.getAllResources().isEmpty());
    }
}
