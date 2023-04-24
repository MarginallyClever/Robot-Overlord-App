package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class DoubleParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        DoubleParameter a = new DoubleParameter("a",1.2);
        DoubleParameter b = new DoubleParameter("b",3.4);
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
