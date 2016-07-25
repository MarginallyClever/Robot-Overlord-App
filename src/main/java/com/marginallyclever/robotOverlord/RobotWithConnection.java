package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.marginallyclever.robotOverlord.communications.AbstractConnection;
import com.marginallyclever.robotOverlord.communications.AbstractConnectionListener;
import com.marginallyclever.robotOverlord.communications.AbstractConnectionManager;


public class RobotWithConnection extends PhysicalObject
implements AbstractConnectionListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1970631551615654640L;
	
	//comms	
	protected transient AbstractConnectionManager connectionManager;
	protected transient String[] portsDetected=null;
	protected transient AbstractConnection connection;
	protected transient boolean isReadyToReceive;
	
	protected transient CollapsiblePanel connectionPanel=null;

	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;

	// connect/rescan/disconnect dialog options
	protected transient JButton buttonConnect;
	
	
	public RobotWithConnection() {
		super();
		isReadyToReceive=false;
		linesTotal=0;
		linesProcessed=0;
		fileOpened=false;
		paused=true;
		running=false;
	}
	
	public AbstractConnectionManager getConnectionManager() {
		return connectionManager;
	}
	public void setConnectionManager(AbstractConnectionManager connectionManager) {
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
		connectionPanel = new CollapsiblePanel("Connection");
		JPanel contents =connectionPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
        buttonConnect = new JButton("Connect");
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
		this.setConnection(null);
		buttonConnect.setText("Connect");
	}
	
	protected void openConnection() {
		JPanel connectionList = new JPanel(new GridLayout(0, 1));
		connectionList.add(new JLabel("Connect"));
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		JComboBox<String> connectionComboBox = new JComboBox<String>();
        connectionList.removeAll();
        connectionList.add(connectionComboBox);
	    
	    String recentConnection = "";
	    if(getConnection()!=null) {
	    	recentConnection = getConnection().getRecentConnection();
	    }

	    if( connectionManager != null ) {
			String [] portsDetected = connectionManager.listConnections();
			int i;
		    for(i=0;i<portsDetected.length;++i) {
		    	connectionComboBox.addItem(portsDetected[i]);
		    	if(recentConnection.equals(portsDetected[i])) {
		    		connectionComboBox.setSelectedIndex(i+1);
		    	}
		    }
	    }
        
		int result = JOptionPane.showConfirmDialog(null, connectionList, "Connect to...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String connectionName = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
			setConnection( connectionManager.openConnection(connectionName) );
			buttonConnect.setText("Disconnect");
		}
	}
	
	public AbstractConnection getConnection() {
		return this.connection;
	}
	
	public void setConnection(AbstractConnection con) {
		if(this.connection!=null) {
			if( this.connection == con ) return;
			this.connection.removeListener(this);
			this.connection.close();
		}
		
		this.connection = con;
		
		if( this.connection != null ) {
			this.connection.addListener(this);
		}
	}
	
	@Override
	public void connectionReady(AbstractConnection arg0) {
		if(arg0==connection && connection!=null) isReadyToReceive=true;
		
		if(isReadyToReceive) {
			isReadyToReceive=false;
			sendFileCommand();
		}
	}

	
	@Override
	public void dataAvailable(AbstractConnection arg0,String data) {
		
	}
	
	/**
	 * tell the robot to move within it's work envelope relative to the robot's current position in the envelope.
	 * @param axis the index of the axis on which to move
	 * @param direction which direction along the axis
	 */
	public void move(int axis,int direction) {
		
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
