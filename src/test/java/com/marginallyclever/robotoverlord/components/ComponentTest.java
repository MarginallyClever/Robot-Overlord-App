package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComponentTest {
    public static void saveAndLoad(Component a, Component b) throws Exception {
        SerializationContext context = new SerializationContext("");
        b.parseJSON(a.toJSON(context),context);
        Assertions.assertEquals(a.toString(),b.toString());
    }

    @Test
    public void saveAndLoad() throws Exception {
        Component a = new PoseComponent();
        Component b = new PoseComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setEnable(false);
        ComponentTest.saveAndLoad(a,b);

        a.setEnable(true);
        ComponentTest.saveAndLoad(a,b);
    }
}
