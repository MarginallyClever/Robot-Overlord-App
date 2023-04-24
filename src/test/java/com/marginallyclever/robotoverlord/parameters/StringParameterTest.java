package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

public class StringParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        StringParameter a = new StringParameter("a","The quick brown fox jumped over the lazy dog");
        StringParameter b = new StringParameter("b","Quoth the raven, 'nevermore.'");
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
