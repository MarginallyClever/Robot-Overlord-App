package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class ComponentTest {
    public static void saveAndLoad(Component a,Component b) throws Exception {
        File temp = File.createTempFile("Component",null);
        temp.deleteOnExit();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            a.save(writer);
        }
        try(BufferedReader reader = new BufferedReader(new FileReader(temp))) {
            b.load(reader);
        }

        Assertions.assertTrue(temp.delete());
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
