package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import org.junit.jupiter.api.Test;

public class TextureEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        TextureEntity a = new TextureEntity("c:/does-not-exist.tmp");
        TextureEntity b = new TextureEntity("");
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
