package com.marginallyclever.robotOverlord;

import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

import java.io.*;

public class MaterialTest {
    //@Test
    public void testLoadAndSave() {
        MaterialEntity m1 = new MaterialEntity();
        @SuppressWarnings("unused")
        MaterialEntity m2;

        File tempFile;
        try {
            tempFile = File.createTempFile("test", "ro");
        } catch (IOException e1) {
            System.out.println("Temp file not created.");
            e1.printStackTrace();
            return;
        }
        tempFile.deleteOnExit();
        try (FileOutputStream fout = new FileOutputStream(tempFile.getCanonicalPath()); ObjectOutputStream objectOut = new ObjectOutputStream(fout)) {
            objectOut.writeObject(m1);
        } catch (java.io.NotSerializableException e) {
            System.out.println("World can't be serialized.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("World save failed.");
            e.printStackTrace();
        }


        try (FileInputStream fin = new FileInputStream(tempFile.getCanonicalPath()); ObjectInputStream objectIn = new ObjectInputStream(fin)) {
            // Create a file input stream

            // Create an object input stream

            // Read an object in from object store, and cast it to a GameWorld
            m2 = (MaterialEntity) objectIn.readObject();
        } catch (IOException e) {
            System.out.println("Material load failed (file io).");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Material load failed (class not found)");
            e.printStackTrace();
        }

        // TODO compare m1 and m2
    }
}
