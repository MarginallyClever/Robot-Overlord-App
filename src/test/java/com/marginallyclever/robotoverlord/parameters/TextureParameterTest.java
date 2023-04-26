package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class TextureParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        TextureParameter a = new TextureParameter("a","c:/does-not-exist.tmp");
        TextureParameter b = new TextureParameter("b",null);
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
