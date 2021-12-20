package com.marginallyclever.robotOverlord;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

import java.io.*;

import org.junit.After;
import org.junit.Before;

public class MaterialTest {
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    //@Test
    public void testLoadAndSave() {
        MaterialEntity m1 = new MaterialEntity();
        @SuppressWarnings("unused")
        MaterialEntity m2;

        File tempFile;
        try {
            tempFile = File.createTempFile("test", "ro");
        } catch (IOException e1) {
            Log.message("Temp file not created.");
            e1.printStackTrace();
            return;
        }
        tempFile.deleteOnExit();
        try (FileOutputStream fout = new FileOutputStream(tempFile.getCanonicalPath()); ObjectOutputStream objectOut = new ObjectOutputStream(fout)) {
            objectOut.writeObject(m1);
        } catch (java.io.NotSerializableException e) {
            Log.message("World can't be serialized.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.message("World save failed.");
            e.printStackTrace();
        }


        try (FileInputStream fin = new FileInputStream(tempFile.getCanonicalPath()); ObjectInputStream objectIn = new ObjectInputStream(fin)) {
            // Create a file input stream

            // Create an object input stream

            // Read an object in from object store, and cast it to a GameWorld
            m2 = (MaterialEntity) objectIn.readObject();
        } catch (IOException e) {
            Log.message("Material load failed (file io).");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.message("Material load failed (class not found)");
            e.printStackTrace();
        }

        // TODO compare m1 and m2
    }
}
