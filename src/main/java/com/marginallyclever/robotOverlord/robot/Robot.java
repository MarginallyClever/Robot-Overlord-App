package com.marginallyclever.robotOverlord.robot;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;


/**
 * A robot visible with a physical presence in the World.  Assumed to have an NetworkConnection to a machine in real life.  
 * @author Dan Royer
 *
 */
public class Robot extends PhysicalObject
implements NetworkConnectionListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1970631551615654640L;
	
	//comms	
	protected transient NetworkConnectionManager connectionManager;
	protected transient String[] portsDetected=null;
	protected transient NetworkConnection connection;
	protected transient boolean isReadyToReceive;
	
	protected transient CollapsiblePanel connectionPanel=null;

	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;
	//private RobotProgram program;

	// connect/rescan/disconnect dialog options
	protected transient JButton buttonConnect;
	
	
	public Robot() {
		super();
		isReadyToReceive=false;
		linesTotal=0;
		linesProcessed=0;
		fileOpened=false;
		paused=true;
		running=false;
		//program=new RobotProgram();
	}
	
	public NetworkConnectionManager getConnectionManager() {
		return connectionManager;
	}
	public void setConnectionManager(NetworkConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	

	public boolean isRunning() { return running; }
	public boolean isPaused() { return paused; }
	public boolean isFileOpen() { return fileOpened; }
	
	
	@Override
	public ArrayList<JPanel> getControlPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getControlPanels(gui);
		list.add(getMenu());
		
		return list;
	}


	protected JPanel getMenu() {
		connectionPanel = new CollapsiblePanel("Robot with connection");
		JPanel contents =connectionPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
        buttonConnect = new JButton(Translator.get("ButtonConnect"));
        buttonConnect.addActionListener(this);

		contents.add(buttonConnect,con1);
		con1.gridy++;

	    return connectionPanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject==buttonConnect) {
			if(connection!=null) {
				closeConnection();
			} else {
				openConnection();
			}
			return;
		}
	}

	
	protected void closeConnection() {
		buttonConnect.setText(Translator.get("ButtonConnect"));
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
	}
	
	protected void openConnection() {
		NetworkConnection s = connectionManager.requestNewConnection(null);
		if(s!=null) {
			buttonConnect.setText(Translator.get("ButtonDisconnect"));
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
	 * stop sending commands to the robot.
	 * @todo add an e-stop command?
	 */
	public void halt() {
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

	/**
	 * Processes a single instruction meant for the robot.
	 * @param line
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
