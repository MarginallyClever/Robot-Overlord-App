package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import org.junit.jupiter.api.Test;

public class ColorEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        ColorEntity a = new ColorEntity("a",0.1,0.2,0.3,0.4);
        ColorEntity b = new ColorEntity("b",0,0,0,0);
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
