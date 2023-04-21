package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class IntParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        IntParameter a = new IntParameter("a",1);
        IntParameter b = new IntParameter("b",2);
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
