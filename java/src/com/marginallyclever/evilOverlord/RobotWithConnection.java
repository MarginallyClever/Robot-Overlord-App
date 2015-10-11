package com.marginallyclever.evilOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.evilOverlord.communications.AbstractConnection;
import com.marginallyclever.evilOverlord.communications.AbstractConnectionManager;
import com.marginallyclever.evilOverlord.communications.AbstractConnectionReadyListener;


public class RobotWithConnection extends PhysicalObject
implements AbstractConnectionReadyListener, ActionListener, ItemListener {
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
	protected transient JPanel connectionList=null;
	protected transient JComboBox<String> connectionComboBox=null;

	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;
	
	private transient boolean dialogResult;  // so dialog boxes can return an ok/cancel
	private transient boolean ignoreSelectionEvents=false;

	// connect/rescan/disconnect dialog options
	protected transient JButton buttonRescan;
	protected transient EvilOverlord gui;

	
	public boolean isRunning() { return running; }
	public boolean isPaused() { return paused; }
	public boolean isFileOpen() { return fileOpened; }
	
	
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
	

	@Override
	public ArrayList<JPanel> getControlPanels(EvilOverlord gui) {
		this.gui=gui;
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
		
		connectionList = new JPanel(new GridLayout(0,1));
		rescanConnections();
		
        buttonRescan = new JButton("Rescan");
        buttonRescan.addActionListener(this);

        contents.add(connectionList,con1);
		con1.gridy++;
		contents.add(buttonRescan,con1);
		con1.gridy++;

	    return connectionPanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		Object subject = e.getSource();
		
		if(subject==buttonRescan) {
			rescanConnections();
			return;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object subject = e.getSource();
		
		if(subject == connectionComboBox) {
			if(ignoreSelectionEvents==false && e.getStateChange()==ItemEvent.SELECTED) {
				if(connectionComboBox.getSelectedIndex()==0) {
					// disconnect
					if(connection!=null) {
						connection.close();
						connection=null;
					}
					rescanConnections();
					return;
				} else {
					String connectionName = connectionComboBox.getItemAt(connectionComboBox.getSelectedIndex());
					connection = connectionManager.openConnection(connectionName);
					connection.addListener(this);
					rescanConnections();
					return;
				}
			}
		}
	}
	
	public void rescanConnections() {
		ignoreSelectionEvents=true;
	    connectionComboBox = new JComboBox<String>();
        connectionComboBox.addItemListener(this);
        connectionList.removeAll();
        connectionList.add(connectionComboBox);
	    
        connectionComboBox.addItem("No connection"); // index 0
	    connectionComboBox.setSelectedIndex(0);
	    
	    String recentConnection = "";
	    if(connection!=null) {
	    	recentConnection = connection.getRecentConnection();
	    }

	    if(connectionManager!=null) {
			portsDetected = connectionManager.listConnections();
			int i;
		    for(i=0;i<portsDetected.length;++i) {
		    	connectionComboBox.addItem(portsDetected[i]);
		    	if(recentConnection.equals(portsDetected[i])) {
		    		connectionComboBox.setSelectedIndex(i+1);
		    	}
		    }
	    }
        ignoreSelectionEvents=false;
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
	 * Take the next line from the file and send it to the robot, if permitted. 
	 */
	public void sendFileCommand() {
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
	
	public void startAt() {
		if(fileOpened && !running) {
			linesProcessed=0;
			if(getStartingLineNumber()) {
				start();
			}
		}
	}
	
	public void pause() {
		if(running) {
			if(paused==true) {
				paused=false;
				// TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
				sendFileCommand();
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
		
		// TODO replace with a more elegant dialog.  See Makelangelo converters for examples.
		final JDialog driver = new JDialog(this.gui.GetMainFrame(),"Start at...");
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
