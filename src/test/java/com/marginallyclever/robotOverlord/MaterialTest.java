package com.marginallyclever.robotOverlord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.marginallyclever.robotOverlord.entity.material.Material;

public class MaterialTest {
	//@Test
	public void testLoadAndSave() {
		Material m1 = new Material();
		@SuppressWarnings("unused")
		Material m2;
		
		File tempFile;
		try {
			tempFile = File.createTempFile("test", "ro");
		} catch (IOException e1) {
			System.out.println("Temp file not created.");
			e1.printStackTrace();
			return;
		}
		tempFile.deleteOnExit();
		FileOutputStream fout=null;
		ObjectOutputStream objectOut=null;
		try {
			fout = new FileOutputStream(tempFile.getCanonicalPath());
			objectOut = new ObjectOutputStream(fout);
			objectOut.writeObject(m1);
		} catch(java.io.NotSerializableException e) {
			System.out.println("World can't be serialized.");
			e.printStackTrace();
		} catch(IOException e) {
			System.out.println("World save failed.");
			e.printStackTrace();
		} finally {
			if(objectOut!=null) {
				try {
					objectOut.close();
				} catch(IOException e) {}
			}
			if(fout!=null) {
				try {
					fout.close();
				} catch(IOException e) {}
			}
		}
		

		FileInputStream fin=null;
		ObjectInputStream objectIn=null;
		try {
			// Create a file input stream
			fin = new FileInputStream(tempFile.getCanonicalPath());
	
			// Create an object input stream
			objectIn = new ObjectInputStream(fin);
	
			// Read an object in from object store, and cast it to a GameWorld
			m2 = (Material) objectIn.readObject();
		} catch(IOException e) {
			System.out.println("Material load failed (file io).");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			System.out.println("Material load failed (class not found)");
			e.printStackTrace();
		} finally {
			if(objectIn!=null) {
				try {
					objectIn.close();
				} catch(IOException e) {}
			}
			if(fin!=null) {
				try {
					fin.close();
				} catch(IOException e) {}
			}
		}
		
		// TODO compare m1 and m2
	}
}
