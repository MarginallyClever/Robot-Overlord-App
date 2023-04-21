package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class ColorParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        ColorParameter a = new ColorParameter("a",0.1,0.2,0.3,0.4);
        ColorParameter b = new ColorParameter("b",0,0,0,0);
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
