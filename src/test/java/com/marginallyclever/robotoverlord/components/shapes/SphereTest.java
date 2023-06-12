package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.components.ComponentTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SphereTest {
    @Test
    public void saveAndLoad() throws Exception {
        Sphere a = new Sphere();
        Sphere b = new Sphere();
        a.radius.set(a.radius.get()+1);
        Assertions.assertNotNull(a.getModel());
        Assertions.assertNotEquals(0,a.getModel().getNumVertices());
        ComponentTest.saveAndLoad(a,b);
    }
}
