package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import java.util.LinkedList;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Wraps all network connection stuff into a neat entity package.
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
	public NetworkConnection connection;
	
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
	int lastNumberAdded=0;
	int nextNumberToSend=0;
	
	
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
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
		resetCommands();
	}
	
	public void openConnection() {
		connection = NetworkConnectionManager.requestNewConnection(null);
		if(connection!=null) {
			connection.addListener(this);
		}
	}

	@Override
	public void update(double dt) {
		if(connection!=null) {
			connection.update();
		}
	}

	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {
		Log.error("REM RESEND "+lineNumber);
		nextNumberToSend=lineNumber;
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
		if(!commands.isEmpty()) {
			sendQueuedMessage(getCommand(nextNumberToSend++));
		}
	}

	@Override
	public void dataAvailable(NetworkConnection arg0, String data) {
		setChanged();
		notifyObservers(data);

		if(data.contains(">")) {
			sendQueuedMessage(getCommand(nextNumberToSend++));
		}
	}

	public boolean isConnectionOpen() {
		return (connection!=null) && (connection.isOpen());
	}
	
	/**
	 * called by someone else to start the process of sending a message
	 * @param command
	 */
	public void sendMessage(String command) {
		if(!isConnectionOpen()) return;
		
		//command.trim();
		if(command.isEmpty()) return;
		
		// add the number for error checking
		//command = "N"+lastNumberAdded+" "+command;
		// add "there is a checksum" (*) + the checksum + end-of-line character
		//command += StringHelper.generateChecksum(command) + "\n";
		
		//commands.add(new NumberedCommand(lastNumberAdded,command));
		//lastNumberAdded++;
		
		sendQueuedMessage(command);
	}
	
	private void sendQueuedMessage(String command) {
		if(command==null) return;
		try {
			connection.sendMessage(command);
		} catch (Exception e) {
			Log.error("RemoteEntity.sendQueuedMessage failed: "+e.getLocalizedMessage());
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
}
