package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import java.io.DataInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Wraps all network connection stuff into a neat entity package designed to send one \n terminated string at a time.
 * 
 * See also https://en.wikipedia.org/wiki/Messaging_pattern
 * 
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class RemoteEntity extends StringEntity implements NetworkConnectionListener {
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final String CUE = "> ";
	static final String NOCHECKSUM = "NOCHECKSUM ";
	static final String BADCHECKSUM = "BADCHECKSUM ";
	static final String BADLINENUM = "BADLINENUM ";
	static final String NEWLINE = "\n";
	static final String COMMENT_START = ";";
	
	private NetworkConnection connection;
	private PrintWriter printWriter;
	private DataInputStream bufferedReader;
	
	public class NumberedCommand {
		public int n;
		public String c;
		
		NumberedCommand(int nn,String cc) {
			n=nn;
			c=cc;
		}
		NumberedCommand() {
			n=0;
			c="";
		}
	}
	
	private LinkedList<NumberedCommand> commands = new LinkedList<NumberedCommand>();
	private int lastNumberAdded=0;
	private int nextNumberToSend=0;
	private boolean waitingForCue = false;
	private ArrayList<Byte> partialMessage = new ArrayList<Byte>();
	
	
	public RemoteEntity() {
		super();
		setName("Connected to");
		resetCommands();
	}
	
	private void resetCommands() {
		commands.clear();
		lastNumberAdded=0;
		nextNumberToSend=0;
	}
	
	private String getCommand(int number) {
		for( NumberedCommand nc : commands ) {
			if(nc.n==number) return nc.c;
		}
		return null;
	}
	
	public void closeConnection() {
		connection=null;
		resetCommands();
		partialMessage.clear();
	}
	
	public void openConnection() {
		connection = NetworkConnectionManager.requestNewConnection(null);
		connection.addListener(this);
		waitingForCue = true;
	}

	@Override
	public void update(double dt) {
		if(connection==null) return;
		
		try {
			int count = bufferedReader.available();
			for(int i=0;i<count;++i) {
				byte c = (byte)bufferedReader.read();
				partialMessage.add(c);
			}
			/*
			{
				String line = lineIter.next();
				int errorNumber = checkForError(line);
				if(errorNumber > -1) {
					Log.error("REM RESEND "+errorNumber);
					nextNumberToSend=errorNumber;
				} else {
					// line is good
					reportDataReceived(line);
					setChanged();
					notifyObservers(line);
	
					// wait for the cue to send another command
					if(line.indexOf(CUE)==0) {
						waitingForCue=false;
					}
				}
			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!waitingForCue && !commands.isEmpty()) {
			sendMessageInternal(getCommand(nextNumberToSend++));
		}
	}


	public boolean isConnectionOpen() {
		return connection!=null;
	}
	
	/**
	 * called by someone else to start the process of sending a message
	 * @param command
	 */
	public void sendMessage(String command) {
		if(!isConnectionOpen()) return;
		if(command.isEmpty()) return;

		// add "there is a checksum" (*) + the checksum + end-of-line character
		command += StringHelper.generateChecksum(command) + "\n";
		
		sendMessageInternal(command);
	}
	
	/**
	 * called by someone else to start the process of sending a message
	 * @param command
	 */
	public void sendMessageGuaranteed(String command) {
		if(!isConnectionOpen()) return;
		
		//command.trim();
		if(command.isEmpty()) return;
		
		// add the number for error checking
		command = "N"+lastNumberAdded+" "+command;
		
		// add "there is a checksum" (*) + the checksum + end-of-line character
		command += StringHelper.generateChecksum(command) + "\n";
		
		// remember the message in case we need to resend it later.
		commands.add(new NumberedCommand(lastNumberAdded,command));
		lastNumberAdded++;
		
		sendMessageInternal(command);
	}
	
	private void sendMessageInternal(String command) {
		if(printWriter==null || command==null) return;
		
		try {
			waitingForCue=true;
			reportDataSent(command);
			printWriter.print(command);
		} catch (Exception e) {
			Log.error("RemoteEntity.sendQueuedMessage failed: "+e.getLocalizedMessage());
		}
	}

	public void reportDataSent(String msg) {
		//if(msg.contains("G0")) return;
		
		Log.message("RemoteEntity SEND " + msg);
	}

	public void reportDataReceived(String msg) {
		if (msg.trim().isEmpty()) return;
		if(msg.contains("D17")) return;
		
		Log.message("RemoteEntity RECV " + msg.trim());
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}

	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataAvailable(NetworkConnection arg0, String data) {
		setChanged();
		notifyObservers(data);
	}
}
