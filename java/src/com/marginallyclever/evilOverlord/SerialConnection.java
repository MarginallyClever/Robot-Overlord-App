package com.marginallyclever.evilOverlord;
import jssc.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SerialConnection
implements SerialPortEventListener, ActionListener {
	private static String cue=">";
	private static String NL="\n";
	
	private String[] portsDetected;
	
	public final static int BAUD_RATE = 57600;
	protected SerialPort serialPort;
	protected boolean portOpened=false;
	protected String portName;
	protected boolean waitingForCue=true;
	
	private String currentPort = "";
	
	// menus & GUIs
	JTextArea log = new JTextArea();
	JScrollPane logPane;
    private JMenuItem [] buttonPorts;
    
    // communications
    String inputBuffer;
    ArrayList<String> commandQueue = new ArrayList<String>();

    // Listeners which should be notified of a change to the percentage.
    private ArrayList<SerialConnectionReadyListener> listeners = new ArrayList<SerialConnectionReadyListener>();

	
	public SerialConnection() {
		detectSerialPorts();
	}
	
	public void finalize() {
		closePort();
		//super.finalize();
	}
	
	public boolean isPortOpened() {
		return portOpened;
	}
	
	public void log(String msg) {
		System.out.print(msg);
		log.append(msg);
		log.setCaretPosition(log.getText().length());
	}
	
	// tell all listeners data has arrived
	private void processLine(String line) {
	      for (SerialConnectionReadyListener listener : listeners) {
	        listener.serialDataAvailable(this,line);
	      }
	}
	
	@Override
	public void serialEvent(SerialPortEvent events) {
		String rawInput, oneLine;
		int x;
		
        if(events.isRXCHAR()) {
        	if(!portOpened) return;
            try {
            	int len = events.getEventValue();
				byte [] buffer = serialPort.readBytes(len);
				if( len>0 ) {
					rawInput = new String(buffer,0,len);
//					Log(rawInput);
					inputBuffer+=rawInput;
					// each line ends with a \n.
					for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
						x=x+1;
						oneLine = inputBuffer.substring(0,x);
						inputBuffer = inputBuffer.substring(x);
						processLine(oneLine);
						// wait for the cue to send another command
						if(oneLine.indexOf(cue)==0) {
							waitingForCue=false;
						}
					}
					if(waitingForCue==false) {
						sendQueuedCommand();
					}
				}
            } catch (SerialPortException e) {}
        }
	}
	
	protected void sendQueuedCommand() {
		if(!portOpened || waitingForCue) return;
		
		if(commandQueue.size()==0) {
		      notifyListeners();
		      return;
		}
		
		String command;
		try {
			command=commandQueue.remove(0);
			String line = command;
			if(line.contains(";")) {
				String [] lines = line.split(";");
				command = lines[0];
			}
			log(command+NL);
			line+=NL;
			serialPort.writeBytes(line.getBytes());
			waitingForCue=true;
		}
		catch(IndexOutOfBoundsException e1) {}
		catch(SerialPortException e2) {}
	}
	
	public void sendCommand(String command) {
		if(!portOpened) return;
		
		commandQueue.add(command);
		sendQueuedCommand();
	}
	
	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}
	
	public boolean readyForCommands() {
		return waitingForCue==false;
	}
	
	/**
	 * Find all available serial ports for the settings->ports menu.
	 */
	public void detectSerialPorts() {
        if(System.getProperty("os.name").equals("Mac OS X")){
        	portsDetected = SerialPortList.getPortNames("/dev/");
            //System.out.println("OS X");
        } else {
        	portsDetected = SerialPortList.getPortNames("COM");
            //System.out.println("Windows");
        }
	}
	
	public boolean doesPortExist(String portName) {
		if(portName==null || portName.equals("")) return false;

		int i;
		for(i=0;i<portsDetected.length;++i) {
			if(portName.equals(portsDetected[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	public void closePort() {
		if(!portOpened) return;
		
	    if (serialPort != null) {
	        try {
		        // Close the port.
		        serialPort.removeEventListener();
		        serialPort.closePort();
	        } catch (SerialPortException e) {
	            // Don't care
	        }
	    }

		portOpened=false;
		currentPort="";
	}
	
	// open a serial connection to a device.  We won't know it's the robot until  
	public int openPort(String portName) {
		if(portOpened) closePort();
		if(doesPortExist(portName) == false) return 0;
		
		closePort();
		
		log("Connecting to "+portName+"..."+NL);

		// open the port
		serialPort = new SerialPort(portName);
		try {
            serialPort.openPort();// Open serial port
            serialPort.setParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
			log("<span style='color:red'>Port could not be configured:"+e.getMessage()+"</span>\n");
			return 3;
		}

		log("<span style='color:green'>Opened.</span>\n");
		portOpened=true;
		currentPort=portName;
		
		return 0;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		int i;
		for(i=0;i<portsDetected.length;++i) {
			if(subject == buttonPorts[i]) {
				openPort(portsDetected[i]);
				return;
			}
		}
	}

    // Adds a listener that should be notified.
    public void addListener(SerialConnectionReadyListener listener) {
      listeners.add(listener);
    }

    // Notifies all the listeners
    private void notifyListeners() {
      for (SerialConnectionReadyListener listener : listeners) {
        listener.serialConnectionReady(this);
      }
    }

	public JMenu getMenu() {
		JMenu subMenu = new JMenu();
	    ButtonGroup group = new ButtonGroup();
	    buttonPorts = new JRadioButtonMenuItem[portsDetected.length];
	    
		int i;
	    for(i=0;i<portsDetected.length;++i) {
	    	buttonPorts[i] = new JRadioButtonMenuItem(portsDetected[i]);
	        if(currentPort.equals(portsDetected[i])) buttonPorts[i].setSelected(true);
	        buttonPorts[i].addActionListener(this);
	        group.add(buttonPorts[i]);
	        subMenu.add(buttonPorts[i]);
	    }
	    
	    return subMenu;
	}
	

	public Component getGUI() {
	    // the log panel
	    log.setEditable(false);
	    log.setForeground(Color.GREEN);
	    log.setBackground(Color.BLACK);
	    logPane = new JScrollPane(log);
	    
	    return logPane;
	}
}
