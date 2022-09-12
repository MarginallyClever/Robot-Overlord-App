package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityTest {
    public static void saveAndLoad(Entity a,Entity b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toString(),b.toString());
    }

    @Test
    public void saveAndLoad() throws Exception {
        Entity a = new Entity();
        Entity b = new Entity();
        EntityTest.saveAndLoad(a,b);

        a.setExpanded(!a.getExpanded());
        EntityTest.saveAndLoad(a,b);
    }
}
