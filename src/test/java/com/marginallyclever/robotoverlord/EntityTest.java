package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;

public class EntityTest {
    public static void saveAndLoad(Entity a,Entity b) throws Exception {
        File temp = File.createTempFile("scene",null);
        temp.deleteOnExit();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            a.save(writer);
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(temp))) {
            b.load(reader);
        }

        temp.delete();

        Assertions.assertEquals(a.toString(),b.toString());
    }

    @Test
    public void saveAndLoad() throws Exception {
        EntityTest.saveAndLoad(new Entity(),new Entity());
    }
}
