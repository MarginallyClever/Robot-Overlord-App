package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.parameters.AbstractEntity;
import org.junit.jupiter.api.Assertions;

public class AbstractEntityTest {
    public static void saveAndLoad(AbstractEntity<?> a, AbstractEntity<?> b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toString(),b.toString());
    }
}
