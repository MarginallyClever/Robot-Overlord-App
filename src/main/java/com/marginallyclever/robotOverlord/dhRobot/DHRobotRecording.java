package com.marginallyclever.robotOverlord.dhRobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DHRobotRecording {
	public boolean isRecording;
	public boolean isPlaying;
	public FileOutputStream recordOutput;
	public FileInputStream recordInput;
	public ObjectOutputStream objectOutputStream;
	public ObjectInputStream objectInputStream;

	DHRobotRecording() {}
	
	public void stop() {
        if(isRecording) setRecording(false);
		if(isPlaying) setPlaying(false);
	}
	
	public void setRecording(boolean newIsRecording) {
		if(isPlaying==true) return;
		if(isRecording==newIsRecording) return;
		
		if(isRecording) {
			try {
				objectOutputStream.flush();
				recordOutput.flush();
				recordOutput.close();
			} catch(Exception e) {
				System.out.println("Recording end failed.");
			}
		} else {
			String directory = System.getProperty("user.home");  
			String fileName = "recording.ro";  
			String absolutePath = directory + File.separator + fileName;
			System.out.println("Recording to "+absolutePath);
			try {
				recordOutput = new FileOutputStream(absolutePath);
			    objectOutputStream = new ObjectOutputStream(recordOutput);
			} catch(Exception e) {
				System.out.println("Recording start failed.");
				return;
			}
		}
		isRecording = newIsRecording;
	}
	
	public void setPlaying(boolean newIsPlaying) {
		if(isPlaying==newIsPlaying) return;
		if(isRecording==true) return;
		
		if(isPlaying) {
			try {
				recordInput.close();
			} catch(Exception e) {
				System.out.println("Playback end failed.");
			}
		} else {
			String directory = System.getProperty("user.home");  
			String fileName = "recording.ro";  
			String absolutePath = directory + File.separator + fileName;

			System.out.println("Playback from "+absolutePath);
			try {
				recordInput = new FileInputStream(absolutePath);
				objectInputStream = new ObjectInputStream(recordInput);

			} catch(Exception e) {
				System.out.println("Playback start failed.");
				return;
			}
		}

		isPlaying = newIsPlaying;
	}
}
