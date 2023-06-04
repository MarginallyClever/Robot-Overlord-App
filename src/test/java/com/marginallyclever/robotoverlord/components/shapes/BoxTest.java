package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.components.ComponentTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoxTest {
    @Test
    public void saveAndLoad() throws Exception {
        Box a = new Box();
        Box b = new Box();
        a.height.set(a.height.get()+1);
        a.width.set(a.width.get()+2);
        a.length.set(a.length.get()+3);
        Assertions.assertNotNull(a.getModel());
        Assertions.assertNotEquals(0,a.getModel().getNumVertices());
        ComponentTest.saveAndLoad(a,b);
    }

}
