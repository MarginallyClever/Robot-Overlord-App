package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Assertions;

public class AbstractParameterTest {
    public static void saveAndLoad(AbstractParameter<?> a, AbstractParameter<?> b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toString(),b.toString());
    }
}
