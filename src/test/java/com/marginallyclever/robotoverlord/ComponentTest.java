package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class ComponentTest {
    public static void saveAndLoad(Component a,Component b) throws Exception {
        b.parseJSON(a.toJSON());
        Assertions.assertEquals(a.toString(),b.toString());
    }

    @Test
    public void saveAndLoad() throws Exception {
        Component a = new Component();
        Component b = new Component();
        ComponentTest.saveAndLoad(a,b);

        a.setEnable(false);
        ComponentTest.saveAndLoad(a,b);

        a.setEnable(true);
        ComponentTest.saveAndLoad(a,b);
    }
}
