package arm5;
import jssc.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

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
	
	public static int BAUD_RATE = 57600;
	public SerialPort serialPort;
	public boolean portOpened=false;
	public boolean portConfirmed=false;
	public String portName;
	public boolean waitingForCue=true;
	
	// settings
	private Preferences prefs;
	
	// menus & GUIs
	JTextArea log = new JTextArea();
	JScrollPane logPane;
    private JMenuItem [] buttonPorts;
    
    // communications
    String inputBuffer;
    ArrayList<String> commandQueue = new ArrayList<String>();

    // Listeners which should be notified of a change to the percentage.
    private ArrayList<SerialConnectionReadyListener> listeners = new ArrayList<SerialConnectionReadyListener>();

	
	public SerialConnection(String name) {
		prefs = Preferences.userRoot().node("SerialConnection").node(name);
		
		DetectSerialPorts();

		OpenPort(GetLastPort());
	}
	
	public void finalize() {
		ClosePort();
		//super.finalize();
	}
	
	private String GetLastPort(){
		return prefs.get("last port","");
	}
	
	private void SetLastPort(String portName) {
		prefs.put("last port", portName);
	}
	
	public void Log(String msg) {
		System.out.print(msg);
		log.append(msg);
		log.setCaretPosition(log.getText().length());
	}

	// override this method to check that the software is connected to the right type of robot.
	public boolean ConfirmPort(String preamble) {
		if(!portOpened) return false;
		
		portConfirmed=true;
		return true;
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
						// wait for the cue to send another command
						if(ConfirmPort(oneLine)) {
							// if we got a > send the next message.
							if(oneLine.indexOf(cue)==0) {
								waitingForCue=false;
							}
						}
					}
					if(waitingForCue==false) {
						SendQueuedCommand();
					}
				}
            } catch (SerialPortException e) {}
        }
	}
	
	protected void SendQueuedCommand() {
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
			Log(command+NL);
			line+=NL;
			serialPort.writeBytes(line.getBytes());
			waitingForCue=true;
		}
		catch(IndexOutOfBoundsException e1) {}
		catch(SerialPortException e2) {}
	}
	
	public void SendCommand(String command) {
		if(!portOpened) return;
		
		commandQueue.add(command);
		if(portConfirmed) {
			SendQueuedCommand();
		}
	}
	
	public void DeleteAllQueuedCommands() {
		commandQueue.clear();
	}
	
	public boolean ReadyForCommands() {
		return waitingForCue==false;
	}
	
	// find all available serial ports for the settings->ports menu.
	public void DetectSerialPorts() {
        if(System.getProperty("os.name").equals("Mac OS X")){
        	portsDetected = SerialPortList.getPortNames("/dev/");
            //System.out.println("OS X");
        } else {
        	portsDetected = SerialPortList.getPortNames("COM");
            //System.out.println("Windows");
        }
	}
	
	public boolean PortExists(String portName) {
		if(portName==null || portName.equals("")) return false;

		int i;
		for(i=0;i<portsDetected.length;++i) {
			if(portName.equals(portsDetected[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	public void ClosePort() {
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
		portConfirmed=false;
	}
	
	// open a serial connection to a device.  We won't know it's the robot until  
	public int OpenPort(String portName) {
		if(portOpened && portName.equals(GetLastPort())) return 0;
		if(PortExists(portName) == false) return 0;
		
		ClosePort();
		
		Log("Connecting to "+portName+"..."+NL);

		// open the port
		serialPort = new SerialPort(portName);
		try {
            serialPort.openPort();// Open serial port
            serialPort.setParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
			Log("<span style='color:red'>Port could not be configured:"+e.getMessage()+"</span>\n");
			return 3;
		}

		Log("<span style='color:green'>Opened.</span>\n");
		portOpened=true;
		SetLastPort(portName);

		return 0;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		int i;
		for(i=0;i<portsDetected.length;++i) {
			if(subject == buttonPorts[i]) {
				OpenPort(portsDetected[i]);
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
        listener.SerialConnectionReady(this);
      }
    }

	public JMenu getMenu() {
		JMenu subMenu = new JMenu();
	    ButtonGroup group = new ButtonGroup();
	    buttonPorts = new JRadioButtonMenuItem[portsDetected.length];
	    
	    String lastPort=GetLastPort();
	    
		int i;
	    for(i=0;i<portsDetected.length;++i) {
	    	buttonPorts[i] = new JRadioButtonMenuItem(portsDetected[i]);
	        if(lastPort.equals(portsDetected[i])) buttonPorts[i].setSelected(true);
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
