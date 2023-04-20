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

    @Test
    public void moveEntity() {
        Entity a = new Entity();
        Entity b = new Entity();
        Entity c = new Entity();
        b.addEntity(a);
        Assertions.assertEquals(b,a.getParent());
        c.addEntity(a);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertFalse(b.getChildren().contains(a));
        b.removeEntity(a);
        Assertions.assertEquals(c,a.getParent());
        Assertions.assertNull(b.getParent());
        Assertions.assertEquals(c,a.getParent());
    }
}
