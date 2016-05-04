package com.marginallyclever.robotOverlord.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Smooth STL models and save them back to disk.  Meant for one time processing files.
 * @author dan royer
 */
public class ModelSmoother {
	public static void main(String[] argv) throws IllegalArgumentException, IOException {
		float vertexEpsilon = 0.1f;
		float normalEpsilon = 0.25f;
		String outputName = "temp.stl";
		
		if(argv.length==0) throw new IllegalArgumentException("not enough parameters");
		
		int i;
		for(i=0;i<argv.length;++i) {
			String sourcName = argv[i];
			Model m = ModelFactory.createModelFromFilename(sourcName);
			m.load();
			m.smoothNormals(vertexEpsilon, normalEpsilon);

			FileOutputStream fos = new FileOutputStream(new File(outputName)); 
			m.saveToStreamAsBinary(fos);
		}
	}
}
