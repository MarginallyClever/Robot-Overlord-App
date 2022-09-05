package com.marginallyclever.robotoverlord.io.serial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.io.Load;

public class SerialLoad implements Load {

	/**
	 * See http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	 * @param filename
	 */
	@Override
	public Entity load(String filename) {
		FileInputStream fin=null;
		ObjectInputStream objectIn=null;
		Entity ent=null;
		try {
			// Create a file input stream
			fin = new FileInputStream(filename);
	
			// Create an object input stream
			objectIn = new ObjectInputStream(fin);
	
			// Read an object in from object store, and cast it to a GameWorld
			ent = (Scene) objectIn.readObject();
		} catch(IOException e) {
			Log.message("World load failed (file io).");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			Log.message("World load failed (class not found)");
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
		return ent;
	}

}
