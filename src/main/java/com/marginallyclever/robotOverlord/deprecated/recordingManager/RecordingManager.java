package com.marginallyclever.robotOverlord.deprecated.recordingManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.RobotOverlord;


@Deprecated
public class RecordingManager {
	static protected boolean isRecording;
	static protected boolean isPlaying;
	
	static public FileOutputStream recordOutput;
	static public ObjectOutputStream objectOutputStream;

	static public FileInputStream recordInput;
	static public ObjectInputStream objectInputStream;

	// number of frames recorded
	static protected long count1=0;
	
	// number of frames played back
	static protected long count2=0;
	
	static public RecordingManagerPanel panel;
	
	static public RecordingManagerPanel getPanel(RobotOverlord arg0) {
		if(panel==null) panel = new RecordingManagerPanel(arg0);
		return panel;
	}
	
	static public void stop() {
        if(isRecording) setRecording(false);
		if(isPlaying) setPlaying(false);
	}
	
	static public void setRecording(boolean newIsRecording) {
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

			if(panel!=null) panel.buttonRecord.setText("Record");
		} else {
			count1=0;

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
			if(panel!=null) panel.buttonRecord.setText("Stop");
		}
		isRecording = newIsRecording;
	}
	
	static public void setPlaying(boolean newIsPlaying) {
		if(isPlaying==newIsPlaying) return;
		if(isRecording==true) return;
		
		if(isPlaying) {
			try {
				recordInput.close();
			} catch(Exception e) {
				System.out.println("Playback end failed.");
			}
			
			if(panel!=null) panel.buttonPlay.setText("Play");
		} else {
			count2=0;

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
			if(panel!=null) panel.buttonPlay.setText("Stop");
		}

		isPlaying = newIsPlaying;
	}
	
	static public void step() {
		//if(isRecording||isPlaying) System.out.println();

    	try {
	        if(isRecording) {
	        	//System.out.print("R"+count1+","+isDirty);
	        	count1++;
	        	objectOutputStream.writeLong(count1);
	        } else if(isPlaying) {
	        	if(recordInput.available()==0) {
	        		System.out.println("** EOF **");
	        		setPlaying(false);
	        		return;
	        	}
	        	count2++;
	        	long frameCount=objectInputStream.readLong();
	        	if(frameCount!=count2) {
	        		throw new IOException("framecount="+frameCount+", count2="+count2);
	        	}
	        	//System.out.print("P"+count2+","+isDirty);
	        }
		} catch (IOException e) {
    		System.out.println("** Recording step error **");
			stop();
		}
	}
	
	static public boolean manageBoolean(boolean arg0) {
		try {
			if(isRecording) objectOutputStream.writeBoolean(arg0);
			else if(isPlaying) arg0 = objectInputStream.readBoolean();
		} catch (IOException e) {
    		e.printStackTrace();
			stop();
		} 
		return arg0;
	}
	
	static public int manageInt(int arg0) {
		try {
			if(isRecording) objectOutputStream.writeInt(arg0);
			else if(isPlaying) arg0 = objectInputStream.readInt();
		} catch (IOException e) {
    		e.printStackTrace();
			stop();
		} 
		return arg0;
	}
	
	static public long manageLong(long arg0) {
		try {
			if(isRecording) objectOutputStream.writeLong(arg0);
			else if(isPlaying) arg0 = objectInputStream.readLong();
		} catch (IOException e) {
    		e.printStackTrace();
			stop();
		} 
		return arg0;
	}
	
	static public double manageDouble(double arg0) {
		try {
			if(isRecording) objectOutputStream.writeDouble(arg0);
			else if(isPlaying) arg0 = objectInputStream.readDouble();
		} catch (IOException e) {
    		e.printStackTrace();
			stop();
		} 
		return arg0;
	}
	
	static public void manageMatrix4d(Matrix4d arg0) {
		try {
			if(isRecording) objectOutputStream.writeUnshared(arg0);
			else if(isPlaying) arg0.set((Matrix4d)objectInputStream.readUnshared());
		} catch (IOException e) {
    		e.printStackTrace();
			stop();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			stop();
		}
	}
	
	static public void manageArrayOfDoubles(double [] arg0) {
		try {
			if(isRecording) {
	        	for(int i=0;i<arg0.length;++i) {
		        	objectOutputStream.writeDouble(arg0[i]);
	        	}
			} else if(isPlaying) {
	        	for(int i=0;i<arg0.length;++i) {
	        		arg0[i]=objectInputStream.readDouble();
	        	}
			}
		} catch (IOException e) {
    		e.printStackTrace();
			stop();
		}
	}
	protected static boolean isRecording() {
		return isRecording;
	}

	protected static boolean isPlaying() {
		return isPlaying;
	}
}
