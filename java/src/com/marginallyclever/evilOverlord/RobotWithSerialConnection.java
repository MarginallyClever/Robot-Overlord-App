package com.marginallyclever.evilOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.marginallyclever.evilOverlord.communications.MarginallyCleverConnection;
import com.marginallyclever.evilOverlord.communications.MarginallyCleverConnectionManager;
import com.marginallyclever.evilOverlord.communications.MarginallyCleverConnectionReadyListener;


public class RobotWithSerialConnection extends ObjectInWorld
implements MarginallyCleverConnectionReadyListener, ActionListener {
	//comms	
	protected MarginallyCleverConnectionManager connectionManager;
	protected String[] portsDetected=null;
	protected MarginallyCleverConnection connection;
	protected boolean isReadyToReceive;
	
	protected JPanel connectionPanel=null;
	protected JPanel connectionList=null;

	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;
	
	private boolean dialogResult;  // so dialog boxes can return an ok/cancel

	MainGUI gui;

	// connect/rescan/disconnect dialog options
	protected JRadioButton [] buttonPorts;
	protected JButton buttonRescan, buttonDisconnect;

	
	public boolean isRunning() { return running; }
	public boolean isPaused() { return paused; }
	public boolean isFileOpen() { return fileOpened; }
	
	
	public RobotWithSerialConnection(MainGUI _gui) {
		super();
		gui = _gui;
		isReadyToReceive=false;
		linesTotal=0;
		linesProcessed=0;
		fileOpened=false;
		paused=true;
		running=false;
	}
	
	public MarginallyCleverConnectionManager getConnectionManager() {
		return connectionManager;
	}
	public void setConnectionManager(MarginallyCleverConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	

	@Override
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list = super.getControlPanels();
		list.add(getMenu());
		
		return list;
	}


	protected JPanel getMenu() {
		connectionList = new JPanel(new GridLayout(0,1));
		rescanConnections();
		
        buttonRescan = new JButton("Rescan Ports");
        buttonRescan.getAccessibleContext().setAccessibleDescription("Rescan the available ports.");
        buttonRescan.addActionListener(this);

        buttonDisconnect = new JButton("Disconnect");
        buttonDisconnect.addActionListener(this);

    	connectionPanel = new JPanel(new GridLayout(0,1));
		connectionPanel.add(new JLabel("Connection"));
		connectionPanel.add(connectionList);
        connectionPanel.add(new JSeparator());
	    connectionPanel.add(buttonRescan);
        connectionPanel.add(buttonDisconnect);
	    
	    return connectionPanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		int i;
		for(i=0;i<buttonPorts.length;++i) {
			if(subject == buttonPorts[i]) {
				connection = connectionManager.openConnection(buttonPorts[i].getText());
				connection.addListener(this);
				rescanConnections();
				return;
			}
		}
		
		if(subject==buttonRescan) {
			rescanConnections();
			return;
		}
		if(subject==buttonDisconnect) {
			if(connection!=null) {
				connection.closeConnection();
				connection=null;
				rescanConnections();
			}
		}
	}

	
	public void rescanConnections() {
		portsDetected = connectionManager.listConnections();
	    buttonPorts = new JRadioButton[portsDetected.length];
	    connectionList.removeAll();
	    
		int i;
	    for(i=0;i<portsDetected.length;++i) {
	    	buttonPorts[i] = new JRadioButton(portsDetected[i]);
	    	if(connection != null && connection.isConnectionOpen() && connection.getRecentConnection().equals(portsDetected[i])) {
	    		buttonPorts[i].setSelected(true);
	    	}
	        buttonPorts[i].addActionListener(this);
	        connectionList.add(buttonPorts[i]);
	    }
	}
	
	
	@Override
	public void serialConnectionReady(MarginallyCleverConnection arg0) {
		if(arg0==connection && connection!=null) isReadyToReceive=true;
		
		if(isReadyToReceive) {
			isReadyToReceive=false;
			SendFileCommand();
		}
	}

	
	@Override
	public void serialDataAvailable(MarginallyCleverConnection arg0,String data) {
		
	}
	
	
	/**
	 * Take the next line from the file and send it to the robot, if permitted. 
	 */
	public void SendFileCommand() {
		if(running==false || paused==true || fileOpened==false || linesProcessed>=linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			line=gcode.get((int)linesProcessed++).trim();
			//previewPane.setLinesProcessed(linesProcessed);
			//statusBar.SetProgress(linesProcessed, linesTotal);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(!sendLineToRobot(line) && linesProcessed<linesTotal);
		
		if(linesProcessed==linesTotal) {
			// end of file
			Halt();
		}
	}

	
	/**
	 * stop sending commands to the robot.
	 * @todo add an e-stop command?
	 */
	public void Halt() {
		running=false;
		paused=false;
	    linesProcessed=0;
	}

	public void Start() {
		paused=false;
		running=true;
		SendFileCommand();
	}
	
	public void StartAt() {
		if(fileOpened && !running) {
			linesProcessed=0;
			if(getStartingLineNumber()) {
				Start();
			}
		}
	}
	
	public void Pause() {
		if(running) {
			if(paused==true) {
				paused=false;
				// TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
				SendFileCommand();
			} else {
				paused=true;
			}
		}
	}
	

	/**
	 * open a dialog to ask for the line number.
	 * @return true if "ok" is pressed, false if the window is closed any other way.
	 */
	private boolean getStartingLineNumber() {
		dialogResult=false;
		
		final JDialog driver = new JDialog(gui.GetMainFrame(),"Start at...");
		driver.setLayout(new GridBagLayout());		
		final JTextField starting_line = new JTextField("0",8);
		final JButton cancel = new JButton(("Cancel"));
		final JButton start = new JButton(("Start"));
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth=2;	c.gridx=0;  c.gridy=0;  driver.add(new JLabel(("Start at line")),c);
		c.gridwidth=2;	c.gridx=2;  c.gridy=0;  driver.add(starting_line,c);
		c.gridwidth=1;	c.gridx=0;  c.gridy=1;  driver.add(cancel,c);
		c.gridwidth=1;	c.gridx=2;  c.gridy=1;  driver.add(start,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					
					if(subject == start) {
						linesProcessed=Integer.decode(starting_line.getText());
						sendLineToRobot("M110 N"+linesProcessed);
						dialogResult=true;
						driver.dispose();
					}
					if(subject == cancel) {
						dialogResult=false;
						driver.dispose();
					}
			  }
		};

		start.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
	    driver.getRootPane().setDefaultButton(start);
		driver.pack();
		driver.setVisible(true);  // modal
		
		return dialogResult;
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
