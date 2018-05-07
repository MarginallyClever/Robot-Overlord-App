package com.marginallyclever.robotOverlord.robot;

import java.util.ArrayList;

import javax.swing.JPanel;

import com.marginallyclever.communications.NetworkConnectionManager;
import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.RobotOverlord;


/**
 * A robot visible with a physical presence in the World.  Assumed to have an NetworkConnection to a machine in real life.  
 * @author Dan Royer
 *
 */
public class Robot extends PhysicalObject implements NetworkConnectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1970631551615654640L;
	
	//comms	
	protected transient String[] portsDetected=null;
	protected transient NetworkConnection connection;
	protected transient boolean isReadyToReceive;


	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;

	private boolean modelsLoaded;
	
	protected transient RobotPanel robotPanel=null;
	
	
	public Robot() {
		super();
		isReadyToReceive=false;
		linesTotal=0;
		linesProcessed=0;
		fileOpened=false;
		paused=true;
		running=false;
		modelsLoaded=false;
		//program=new RobotProgram();
	}
	

	public boolean isRunning() { return running; }
	public boolean isPaused() { return paused; }
	public boolean isFileOpen() { return fileOpened; }
	
	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(robotPanel == null) robotPanel = new RobotPanel(gui,this);
		list.add(robotPanel);
		
		return list;
	}
	
	protected void closeConnection() {
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
	}
	
	protected void openConnection() {
		NetworkConnection s = NetworkConnectionManager.requestNewConnection(null);
		if(s!=null) {
			setConnection(s);
		}
	}
	
	public NetworkConnection getConnection() {
		return this.connection;
	}
	
	public void setConnection(NetworkConnection arg0) {
		if(connection!=null && connection!=arg0) {
			closeConnection();
		}
		
		connection = arg0;
		
		if( connection != null ) {
			connection.addListener(this);
		}
	}

	
	@Override
	public void dataAvailable(NetworkConnection arg0,String data) {
		if(arg0==connection && connection!=null) {
			if(data.startsWith(">")) {
				isReadyToReceive=true;
			}
		}
		
		if(isReadyToReceive) {
			sendFileCommand();
		}
		System.out.println(data);
	}
	
	/**
	 * tell the robot to move within it's work envelope relative to the robot's current position in the envelope.
	 * @param axis the index of the axis on which to move
	 * @param direction which direction along the axis
	 */
	public void move(int axis,int direction) {

		isReadyToReceive=false;
	}
	
	/**
	 * Take the next line from the file and send it to the robot, if permitted. 
	 */
	public void sendFileCommand() {
		if(!running || paused || !fileOpened || linesProcessed>=linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			line=gcode.get((int)linesProcessed++).trim();
			//previewPane.setLinesProcessed(linesProcessed);
			//statusBar.SetProgress(linesProcessed, linesTotal);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(!sendLineToRobot(line) && linesProcessed<linesTotal);

		isReadyToReceive=false;
		
		if(linesProcessed==linesTotal) {
			// end of file
			halt();
		}
	}

	
	/**
	 * Stop sending commands to the robot.
	 */
	public void halt() {
		// TODO add an e-stop command?
		running=false;
		paused=false;
	    linesProcessed=0;
	}

	public void start() {
		paused=false;
		running=true;
		sendFileCommand();
	}
	
	public void startAt(int lineNumber) {
		if(fileOpened && !running) {
			linesProcessed=lineNumber;
			start();
		}
	}
	
	public void pause() {
		if(running) {
			if(paused) {
				paused=false;
				// TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
				sendFileCommand();
			} else {
				paused=true;
			}
		}
	}
	
	// Must be called by subclass to loadModels on render.
	public void render(GL2 gl2) {
		if(!modelsLoaded) {
			loadModels(gl2);
			modelsLoaded=true;
		}
	}

	// stub to be overridden by subclasses.
	protected void loadModels(GL2 gl2) {}
	
	/**
	 * Pr)ocesses a single instruction meant for the robot.
	 * @param line command to send
	 * @return true if the command is sent to the robot.
	 */
	public boolean sendLineToRobot(String line) {
		if(connection==null) return false;

		// contains a comment?  if so remove it
		int index=line.indexOf('(');
		if(index!=-1) {
			//String comment=line.substring(index+1,line.lastIndexOf(')'));
			//Log("* "+comment+NL);
			line=line.substring(0,index).trim();
			if(line.length()==0) {
				// entire line was a comment.
				return false;  // still ready to send
			}
		}

		// send relevant part of line to the robot
		try{
			connection.sendMessage(line);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * confirm that this RobotKeyframe is valid (robot is not in an impossible state)
	 * @param the RobotKeyframe to validate
	 * @return true if the state is valid
	 */
	public boolean isKeyframeValid(RobotKeyframe arg0) {
		return false;
	}
	
	/**
	 * interpolate between two keyframes.
	 * @param a starting state
	 * @param b ending state
	 * @param scale value from 0 to 1, inclusive.
	 * @return the interpolated keyframe = (b-a)*scale + a
	 */
	public RobotKeyframe interpolateKeyframes(RobotKeyframe a,RobotKeyframe b,float scale) {
		return a;//( b - a ) * scale + a;
	}
	
/*
	// pull the last connected port from prefs
	private void loadRecentPortFromPreferences() {
		recentPort = prefs.get("recent-port", "");
	}

	// update the prefs with the last port connected and refreshes the menus.
	public void setRecentPort(String portName) {
		prefs.put("recent-port", portName);
		recentPort = portName;
		//UpdateMenuBar();
	}
*/
}
