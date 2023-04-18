package com.marginallyclever.communications.transport.tcp;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.transport.TransportLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;


/**
 * SSH TCP/IP connection to a Raspberry Pi and then open a picocom session to /dev/ttyACM0
 * @author Dan Royer 
 * @since 1.6.0 (2020-04-08)
 */
public final class TCPSession extends SessionLayer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TCPSession.class);
    private static final String SHELL_TO_SERIAL_COMMAND = " ~/Robot-Overlord-App/arduino/connect.sh";
	private static final int DEFAULT_TCP_PORT = 22;
    
    private JSch jsch=new JSch();
    private Session session;
    private ChannelExec channel;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    
	private TransportLayer transportLayer;
	private String connectionName = "";
	private boolean portOpened = false;
	private Thread thread;
	private boolean keepPolling;

	
	private String inputBuffer = "";

	
	public TCPSession(TransportLayer layer) {
		super();
		transportLayer = layer;
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
		session.setUserInfo(new SSHUserInfo());
	    session.setPassword(userParts[1]);
	    session.connect(30000);   // making a connection with timeout.

	    channel = (ChannelExec)session.openChannel("exec");
	    logger.info("Sending "+SHELL_TO_SERIAL_COMMAND);
	    channel.setCommand(SHELL_TO_SERIAL_COMMAND);
	    channel.connect();
	    // remember the data streams
	    inputStream = new BufferedReader(new InputStreamReader(channel.getInputStream()));
	    outputStream = new PrintWriter(channel.getOutputStream());
	    
		connectionName = ipAddress;
		portOpened = true;
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
			outputStream.close();
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			channel.disconnect();
			channel = null;
			inputStream=null;
			outputStream=null;
			session.disconnect();
			session=null;
		}
		portOpened = false;
	}
	
	// Begins when Thread.start() is called in the constructor
	@Override
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

	protected void dataAvailable(int len,String message) {
		if( !portOpened || len==0 ) return;
		
		inputBuffer += message;
		
		// each line ends with a \n.
		int x;

		for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
			// include \n in line.
			++x;
			// extract the line
			String oneLine = inputBuffer.substring(0,x).trim();
			inputBuffer = inputBuffer.substring(x);

			if(oneLine.isEmpty()) return;
			
			reportDataReceived(oneLine);
			notifyListeners(new SessionLayerEvent(this, SessionLayerEvent.DATA_AVAILABLE,oneLine));
		}
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		if( !portOpened || outputStream==null ) return;

		try {
			outputStream.write(msg);
			outputStream.flush();
			reportDataSent(msg);
		}
		catch(IndexOutOfBoundsException e1) {
			notifyListeners(new SessionLayerEvent(this, SessionLayerEvent.TRANSPORT_ERROR,e1.getLocalizedMessage()));
		}
	}

	public void reportDataSent(String msg) {
		//logger.info("TCPConnection SEND " + msg.trim());
	}

	public void reportDataReceived(String msg) {
		//logger.info("TCPConnection RECV " + msg.trim());
	}

	// connect to the last port
	@Override
	public void reconnect() throws Exception {
		openConnection(connectionName);
	}

	/**
	 * @return the port open for this connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}

	@Override
	public String getName() {
		return connectionName;
	}

	@Override
	public TransportLayer getTransportLayer() {
		return this.transportLayer;
	}
}
