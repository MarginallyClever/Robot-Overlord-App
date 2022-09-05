package com.marginallyclever.robotoverlord.io.serial;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.io.Save;

public class SerialSave implements Save {

	/**
	 * See http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	 * @param filename
	 * @param ent
	 */
	@Override
	public void save(String filename, Entity ent) {
		FileOutputStream fout=null;
		ObjectOutputStream objectOut=null;
		try {
			fout = new FileOutputStream(filename);
			objectOut = new ObjectOutputStream(fout);
			objectOut.writeObject(ent);
		} catch(java.io.NotSerializableException e) {
			Log.message("Something can't be serialized.");
			e.printStackTrace();
		} catch(IOException e) {
			Log.message("Save failed.");
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
	}

}
