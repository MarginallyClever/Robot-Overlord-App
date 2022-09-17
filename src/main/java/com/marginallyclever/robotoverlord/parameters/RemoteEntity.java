package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.communications.NetworkSessionManager;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Wraps all network connection stuff into a neat entity package designed to send one \n terminated string at a time.
 * In the OSI Model of computer networking (https://en.wikipedia.org/wiki/OSI_model#Layer_5:_Session_Layer) this is
 * the Presentation layer.
 * 
 * See also https://en.wikipedia.org/wiki/Messaging_pattern
 * 
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class RemoteEntity extends StringEntity implements NetworkSessionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2553138173639792442L;
	
	// the firmware uses a specific syntax.  these are elements of that syntax
	static final String CUE = "> ";
	static final String NOCHECKSUM = "NOCHECKSUM ";
	static final String BADCHECKSUM = "BADCHECKSUM ";
	static final String BADLINENUM = "BADLINENUM ";
	static final String NOLINENUM = "NOLINENUM ";
	static final String NEWLINE = "\n";
	static final String COMMENT_START = ";";
	
	public class NumberedCommand {
		// the line number for this command
		public int n;
		// the command text
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

	private transient NetworkSession networkSession;
	
	private transient int lastNumberAdded=0;
	private transient int nextNumberToSend=0;
	private transient boolean waitingForCue = false;
	// data being sent
	private transient LinkedList<NumberedCommand> commands = new LinkedList<NumberedCommand>();
	// data received
	private transient ArrayList<Byte> partialMessage = new ArrayList<Byte>();

	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		commands = new LinkedList<NumberedCommand>();
		partialMessage = new ArrayList<Byte>();
	}
	
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
		if(networkSession!=null) {
			networkSession.closeConnection();
			networkSession=null;
		}
		resetCommands();
		partialMessage.clear();
	}
	
	public void openConnection() {
		Entity e = getRoot();
		Component parent = (e instanceof RobotOverlord) ? ((RobotOverlord)e).getMainFrame() : null;
		
		networkSession = NetworkSessionManager.requestNewSession(parent);
		if(networkSession!=null) {
			networkSession.addListener(this);
			waitingForCue = true;
		}
	}

	@Override
	public void update(double dt) {
		if(networkSession==null) return;
		
		if(!waitingForCue && !commands.isEmpty()) {
			if(commands.get(commands.size()-1).n > nextNumberToSend) {
				sendMessageInternal(getCommand(nextNumberToSend++));
			}
		}
	}

	public boolean isConnectionOpen() {
		return networkSession!=null;
	}
	
	/**
	 * Send a message that we don't need to guarantee will arrive.
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
	 * Send a message that we guarantee will arrive.
	 * @param command
	 */
	public void sendMessageGuaranteed(String command) {
		if(!isConnectionOpen()) return;
		
		command = command.trim();
		if(command.isEmpty()) return;
		
		// add the number for error checking
		command = "N"+lastNumberAdded+" "+command;
		
		// add "there is a checksum" (*) + the checksum + end-of-line character
		command += StringHelper.generateChecksum(command) + "\n";
		
		// remember the message in case we need to resend it later.
		commands.add(new NumberedCommand(lastNumberAdded,command));
		lastNumberAdded++;

		if(!waitingForCue ) {
			if(commands.get(commands.size()-1).n > nextNumberToSend) {
				sendMessageInternal(getCommand(nextNumberToSend++));
			}
		}
	}
	
	private void sendMessageInternal(String command) {
		if( command==null || waitingForCue ) return;
		
		try {
			waitingForCue=true;
			reportDataSent(command);
			networkSession.sendMessage(command);
		} catch (Exception e) {
			Log.error(e.getLocalizedMessage());
		}
	}

	public void reportDataSent(String msg) {
		//if(msg.contains("G0")) return;
		
		Log.message("RemoteEntity SEND " + msg.trim());
	}

	public void reportDataReceived(String msg) {
		if(msg.trim().isEmpty()) return;
		if(msg.contains("D17")) return;
		
		Log.message("RemoteEntity RECV " + msg.trim());
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
				Log.error("BADCHECKSUM "+x);
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
		if (line.lastIndexOf(NOLINENUM) != -1) {
			Log.error("NOLINENUM");
			return 0;
		}

		return -1;
	}

	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {
		if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) receiveData((NetworkSession)evt.getSource(),(String)evt.data);
		else if(evt.flag == NetworkSessionEvent.TRANSPORT_ERROR) transportError((NetworkSession)evt.getSource(),(String)evt.data);
	}
	
	private void receiveData(NetworkSession arg0, String data) {
		reportDataReceived(data);
		
		if (data.startsWith("> ")) {
			waitingForCue=false;
		}

		// check for error
		int error_line = errorReported(data);
		if(error_line != -1) {
			nextNumberToSend = error_line;
		} else {
			// no error
			if(!data.trim().equals(CUE.trim())) {
				notifyPropertyChangeListeners(new PropertyChangeEvent(this, "data", null, data));
			}
		}
	}

	private void transportError(NetworkSession arg0, String errorMessage) {
		Log.error("RemoteEntity error: "+errorMessage);
		arg0.closeConnection();
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
