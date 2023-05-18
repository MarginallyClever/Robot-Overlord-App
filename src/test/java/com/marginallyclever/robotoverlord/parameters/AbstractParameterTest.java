package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.junit.jupiter.api.Assertions;

public class AbstractParameterTest {
    public static void saveAndLoad(AbstractParameter<?> a, AbstractParameter<?> b) throws Exception {
        SerializationContext context = new SerializationContext("");
        b.parseJSON(a.toJSON(context),context);
        Assertions.assertEquals(a.toString(),b.toString());
    }
}
