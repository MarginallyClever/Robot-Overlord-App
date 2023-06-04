package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.components.ComponentTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DecalTest {
    @Test
    public void saveAndLoad() throws Exception {
        Decal a = new Decal();
        Decal b = new Decal();
        a.height.set(a.height.get()+1);
        a.width.set(a.width.get()+2);
        Assertions.assertNotNull(a.getModel());
        Assertions.assertNotEquals(0,a.getModel().getNumVertices());
        ComponentTest.saveAndLoad(a,b);
    }
}
