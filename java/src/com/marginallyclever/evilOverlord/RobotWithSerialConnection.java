package com.marginallyclever.evilOverlord;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class RobotWithSerialConnection extends ObjectInWorld
implements SerialConnectionReadyListener {
	//comms	
	protected SerialConnection connection;
	private boolean arduinoReady;

	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;
	
	private boolean dialog_result;  // so dialog boxes can return an ok/cancel

	MainGUI gui;
	
	
	public boolean isRunning() { return running; }
	public boolean isPaused() { return paused; }
	public boolean isFileOpen() { return fileOpened; }
	
	
	public RobotWithSerialConnection(MainGUI _gui) {
		super();
		gui = _gui;
		connection = new SerialConnection();
		connection.addListener(this);
		arduinoReady=false;
		linesTotal=0;
		linesProcessed=0;
		fileOpened=false;
		paused=true;
		running=false;
	}
	

	@Override
	public void serialConnectionReady(SerialConnection arg0) {
		if(arg0==connection) arduinoReady=true;
		
		if(arduinoReady) {
			arduinoReady=false;
			SendFileCommand();
		}
	}

	
	@Override
	public void serialDataAvailable(SerialConnection arg0,String data) {
		
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
		} while(!SendLineToRobot(line) && linesProcessed<linesTotal);
		
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
		dialog_result=false;
		
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
						SendLineToRobot("M110 N"+linesProcessed);
						dialog_result=true;
						driver.dispose();
					}
					if(subject == cancel) {
						dialog_result=false;
						driver.dispose();
					}
			  }
		};

		start.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
	    driver.getRootPane().setDefaultButton(start);
		driver.pack();
		driver.setVisible(true);  // modal
		
		return dialog_result;
	}

	/**
	 * Processes a single instruction meant for the robot.
	 * @param line
	 * @return true if the command is sent to the robot.
	 */
	public boolean SendLineToRobot(String line) {
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
		connection.sendCommand(line);
		
		return true;
	}
}
