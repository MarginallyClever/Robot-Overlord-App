package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class BooleanEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        BooleanEntity a = new BooleanEntity("a",true);
        BooleanEntity b = new BooleanEntity("b",false);
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
