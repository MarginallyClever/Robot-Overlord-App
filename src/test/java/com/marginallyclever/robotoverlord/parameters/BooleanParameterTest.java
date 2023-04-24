package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class BooleanParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        BooleanParameter a = new BooleanParameter("a",true);
        BooleanParameter b = new BooleanParameter("b",false);
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
