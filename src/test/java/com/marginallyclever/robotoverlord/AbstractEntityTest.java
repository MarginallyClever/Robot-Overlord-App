package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;

import java.io.*;

public class AbstractEntityTest {
    public static void saveAndLoad(AbstractEntity a, AbstractEntity b) throws Exception {
        File temp = File.createTempFile("AbstractEntity",null);
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
}
