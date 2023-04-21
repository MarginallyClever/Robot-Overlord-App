package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class DoubleEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        DoubleEntity a = new DoubleEntity("a",1.2);
        DoubleEntity b = new DoubleEntity("b",3.4);
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
