package arm5;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class RecordAndPlayback {
	boolean isRecording=false;
	OutputStream outputStream;
	
	boolean isPlayback=false;
	InputStream inputStream;

	
	public void StartRecord(String filename) {
		if(isRecording || isPlayback) {
			// TODO throw exception - programmer error?
			return;
		}
		
		//String outputFile = System.getProperty("java.io.tmpdir") + "/" + "robottrainer-temp.ngc";
		System.out.println("output file = "+filename);
		try {
			outputStream = new FileOutputStream(filename);
		}
		catch(IOException ex) {
			System.out.println(ex.getLocalizedMessage());
			return;
		}
		isRecording=true;
	}

	public void StartPlayback(String filename) {
		if(isRecording || isPlayback) {
			// TODO throw exception - programmer error?
			return;
		}
		
		//String outputFile = System.getProperty("java.io.tmpdir") + "/" + "robottrainer-temp.ngc";
		System.out.println("output file = "+filename);
		try {
			inputStream = new FileInputStream(filename);
		}
		catch(IOException ex) {
			System.out.println(ex.getLocalizedMessage());
			return;
		}
		isRecording=true;
	}

	public void Update() {
		if(isRecording) {
			UpdateRecording();
		} else if(isPlayback) {
			UpdatePlayback();
			
		}
	}
	
	protected void UpdateRecording() {
/*		try {
			// catch input and dump to the file
			
			outputStream.write();
		}
		catch(IOException ex) {
			System.out.println(ex.getLocalizedMessage());
			EndRecording();
		}*/
	}
	
	protected void UpdatePlayback() {
		try {
			// read input and ... ?

			inputStream.read();
		}
		catch(IOException ex) {
			System.out.println(ex.getLocalizedMessage());
			EndPlayback();
		}
	}
	
	public void End() {
		if(isRecording) {
			EndRecording();
		} else if(isPlayback) {
			EndPlayback();
		}
	}
	
	protected void EndRecording() {
		try {
			outputStream.close();
		}
		catch(IOException ex) {
			System.out.println(ex.getLocalizedMessage());
			return;
		}
		isRecording=false;
	}
	
	protected void EndPlayback() {
		try {
			inputStream.close();
		}
		catch(IOException ex) {
			System.out.println(ex.getLocalizedMessage());
			return;
		}
		isPlayback=false;
	}
}
