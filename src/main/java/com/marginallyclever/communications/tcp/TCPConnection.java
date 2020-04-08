package com.marginallyclever.communications.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.robotOverlord.log.Log;


/**
 * SSH TCP/IP connection to a Raspberry Pi and then open a picocom session to /dev/ttyACM0
 * @author Dan Royer 
 * @since 1.6.0 (2020-04-08)
 */
public final class TCPConnection extends NetworkConnection implements Runnable {
	private static final String DEFAULT_BAUD = "57600";
	private static final String DEFAULT_USB_DEVICE = "/dev/ttyACM0";
	
    private static final String SHELL_TO_SERIAL_STRING = "picocom -b"+DEFAULT_BAUD+" "+DEFAULT_USB_DEVICE;
    //private static final String SHELL_TO_SERIAL_STRING = "screen "+DEFAULT_USB_DEVICE; maybe?
    
    private JSch jsch=new JSch();
    private Session session;
    private ChannelExec channel;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    
    
	private TransportLayer transportLayer;
	private String connectionName = "";
	private boolean portOpened = false;
	private boolean waitingForCue = false;
	private Thread thread;
	private boolean keepPolling;


	static final String CUE = "> ";
	static final String NOCHECKSUM = "NOCHECKSUM ";
	static final String BADCHECKSUM = "BADCHECKSUM ";
	static final String BADLINENUM = "BADLINENUM ";
	static final String NEWLINE = "\n";
	static final String COMMENT_START = ";";
	private static final int DEFAULT_TCP_PORT = 22;
	
	private String inputBuffer = "";
	ArrayList<String> commandQueue = new ArrayList<String>();

	
	public TCPConnection(TransportLayer layer) {
		super();
		transportLayer = layer;
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		commandQueue.add(msg);
		sendQueuedCommand();
	}

	/** 
	 * Open a connection to a device on the net.
	 * The ipAddress format is a normal URI - name:password@ipaddress:port
	 * @param ipAddress the network address of the device
	 */
	@Override
	public void openConnection(String ipAddress) throws Exception {
		if (portOpened) return;

		closeConnection();

		jsch.setKnownHosts("./.ssh/known_hosts");
		
		if(ipAddress.startsWith("http://")) {
			ipAddress = ipAddress.substring(7);
		}

		// the string input
		URL a = new URL("http://"+ipAddress);
		String host = a.getHost();
		int port = a.getPort();
		String userInfo = a.getUserInfo();
		if(port==-1) port = DEFAULT_TCP_PORT;

		String [] userParts = userInfo.split(":");
		
		// now we have everything we need
		
		session = jsch.getSession(userParts[0], host, port);
	    session.setPassword(userParts[1]);
	    session.connect(30000);   // making a connection with timeout.

	    channel = (ChannelExec)session.openChannel("exec");
	    Log.message("Sending "+SHELL_TO_SERIAL_STRING);
	    channel.setCommand(SHELL_TO_SERIAL_STRING);
	    channel.connect();
	    // remember the data streams
	    inputStream = new BufferedReader(new InputStreamReader(channel.getInputStream()));
	    outputStream = new PrintWriter(channel.getOutputStream());
	    
		connectionName = ipAddress;
		portOpened = true;
		waitingForCue = true;
		keepPolling=true;

		thread = new Thread(this);
		thread.start();
	}


	@Override
	public void closeConnection() {
		if (!portOpened) return;
		if (channel != null) {
			keepPolling=false;

			outputStream.flush();
			channel.disconnect();
			channel = null;
			inputStream=null;
			outputStream=null;
			session.disconnect();
			session=null;
		}
		portOpened = false;
	}
	
	/**
	 * Begins when Thread.start() is called in the constructor
	 */
	public void run() {
		StringBuilder input = new StringBuilder();
		
		while(keepPolling) {
			try {
				if(inputStream.ready()) {
					input.append((char)inputStream.read());
				}
				String inputAsString = input.toString();
				if(inputAsString.endsWith("\n")) {
					dataAvailable(inputAsString.length(),inputAsString);
					input.setLength(0);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				closeConnection();
			}
		}
	}
	
	
	/**
	 * Check if the robot reports an error and if so what line number.
	 * @param line the message from the robot to be parsed
	 * @return -1 if there was no error, otherwise the line number containing the error.
	 */
	protected int errorReported(String line) {
		if (line.lastIndexOf(NOCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(NOCHECKSUM) + NOCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("NOCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADLINENUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADLINENUM) + BADLINENUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADLINENUM "+err);
			} catch (Exception e) {}

			return err;
		}

		return -1;
	}


	public void dataAvailable(int len,String message) {
		if( !portOpened || len==0 ) return;
		
		inputBuffer += message;
		
		// each line ends with a \n.
		int x;

		for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
			// include \n in line.
			++x;
			// extract the line
			String oneLine = inputBuffer.substring(0,x);
			inputBuffer = inputBuffer.substring(x);

			// check for error
			int error_line = errorReported(oneLine);
			if(error_line != -1) {
				notifyLineError(error_line);
			} else {
				// no error
				if(!oneLine.trim().equals(CUE.trim())) {
					notifyDataAvailable(oneLine);
				}
			}

			// wait for the cue to send another command
			if(oneLine.indexOf(CUE)==0) {
				waitingForCue=false;
			}
		}
		if(waitingForCue==false) {
			sendQueuedCommand();
		}
	}


	protected void sendQueuedCommand() {
		if(!portOpened || waitingForCue) return;

		if(commandQueue.isEmpty()==true) {
			notifySendBufferEmpty();
			return;
		}

		try {
			String line=commandQueue.remove(0);
			// trim comments
			if(line.contains(COMMENT_START)) {
				String [] lines = line.split(COMMENT_START);
				line = lines[0];
			}
			// make sure there's a newline
			if(line.endsWith("\n") == false) {
				line+=NEWLINE;
			}
			outputStream.write(line);
			waitingForCue=true;
		}
		catch(IndexOutOfBoundsException e1) {}
	}

	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}

	// connect to the last port
	@Override
	public void reconnect() throws Exception {
		openConnection(connectionName);
	}

	/**
	 * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
	 *
	 * @param src
	 * @return the portion of the string that is actually a number
	 */
	private String getNumberPortion(String src) {
		src = src.trim();
		int length = src.length();
		String result = "";
		for (int i = 0; i < length; i++) {
			Character character = src.charAt(i);
			if (Character.isDigit(character)) {
				result += character;
			}
		}
		return result;
	}

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}

	@Override
	public String getRecentConnection() {
		return connectionName;
	}

	@Override
	public TransportLayer getTransportLayer() {
		return this.transportLayer;
	}
	
	@Override
	public void update() {}
}
