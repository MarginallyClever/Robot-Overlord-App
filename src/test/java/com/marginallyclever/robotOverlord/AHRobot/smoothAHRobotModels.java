package com.marginallyclever.robotOverlord.AHRobot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.marginallyclever.robotOverlord.Model;

public class smoothAHRobotModels {
	private void saveModelToFile(String inName,String outName) throws IOException {
		Model m = Model.createModelFromFilename(inName);
		m.load();
		m.smoothNormals(0.1f, 0.25f);
		
		File file = new File(outName);
		FileOutputStream fos = new FileOutputStream(file);
		m.saveToStreamAsBinary(fos);
	}
	
	@Test
	public void smoothModels() throws IllegalStateException, IOException {
		String wd = System.getProperty("user.dir");
		System.out.println("Working directory="+wd);
		System.out.println("hand");			saveModelToFile("/AH/WristRot.stl",		wd + "/AH/WristRot-smooth.stl");
		System.out.println("anchor");		saveModelToFile("/AH/rotBaseCase.stl",	wd + "/AH/rotBaseCase-smooth.stl");
		System.out.println("shoulder");		saveModelToFile("/AH/Shoulder_r1.stl",	wd + "/AH/Shoulder_r1-smooth.stl");
		System.out.println("elbow");		saveModelToFile("/AH/Elbow.stl",		wd + "/AH/Elbow-smooth.stl");
		System.out.println("forwarm");		saveModelToFile("/AH/Forearm.stl",		wd + "/AH/Forearm-smooth.stl");
		System.out.println("wrist");		saveModelToFile("/AH/Wrist_r1.stl",		wd + "/AH/Wrist_r1-smooth.stl");
	}
	
	@Test
	public void loadAndSave() {}
}
