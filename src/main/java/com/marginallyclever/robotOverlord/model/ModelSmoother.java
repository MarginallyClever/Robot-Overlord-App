package com.marginallyclever.robotOverlord.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * Smooth STL models and save them back to disk.  Meant for one time processing files.
 * @author dan royer
 */
public class ModelSmoother {/*
	public static void main(String[] argv) throws IllegalArgumentException, IOException {
		float vertexEpsilon = 0.1f;
		float normalEpsilon = 0.25f;
		
		if( argv.length     == 0 ) throw new IllegalArgumentException("not enough parameters");
		if( argv.length % 2 != 0 ) throw new IllegalArgumentException("not enough parameters");
		
		int i;
		for(i=0;i<argv.length;++i) {
			String sourceName = argv[i+0];
			String destName   = argv[i+1];
			saveModelToFile(sourceName,destName,vertexEpsilon,normalEpsilon);
		}
	}*/

	private static void saveModelToFile(String inName,String outName,float vertexEpsilon,float normalEpsilon) throws IOException {
		Model m = ModelFactory.createModelFromFilename(inName);
		m.load();
		m.smoothNormals(vertexEpsilon,normalEpsilon);
		
		File file = new File(outName);
		FileOutputStream fos = new FileOutputStream(file);
		m.saveToStreamAsBinary(fos);
	}
	
	@Test
	public void smoothAll() throws IOException {
		float vertexEpsilon = 0.1f;
		float normalEpsilon = 0.25f;
		String wd = System.getProperty("user.dir");
		System.out.println("Working directory="+wd);
		
		System.out.println("hand");			saveModelToFile("/AH/WristRot.stl",		wd + "/AH/WristRot-smooth.stl2",		vertexEpsilon,normalEpsilon);
		System.out.println("anchor");		saveModelToFile("/AH/rotBaseCase.stl",	wd + "/AH/rotBaseCase-smooth.stl2",	vertexEpsilon,normalEpsilon);
		System.out.println("shoulder");		saveModelToFile("/AH/Shoulder_r1.stl",	wd + "/AH/Shoulder_r1-smooth.stl2",	vertexEpsilon,normalEpsilon);
		System.out.println("elbow");		saveModelToFile("/AH/Elbow.stl",		wd + "/AH/Elbow-smooth.stl2",		vertexEpsilon,normalEpsilon);
		System.out.println("forearm");		saveModelToFile("/AH/Forearm.stl",		wd + "/AH/Forearm-smooth.stl2",		vertexEpsilon,normalEpsilon);
		System.out.println("wrist");		saveModelToFile("/AH/Wrist_r1.stl",		wd + "/AH/Wrist_r1-smooth.stl2",		vertexEpsilon,normalEpsilon);
	}
}
