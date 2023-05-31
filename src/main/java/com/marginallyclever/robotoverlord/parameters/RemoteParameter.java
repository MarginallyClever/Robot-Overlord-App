package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.session.SessionLayerEvent;
import com.marginallyclever.communications.session.SessionLayerListener;
import com.marginallyclever.communications.session.SessionLayerManager;
import com.marginallyclever.convenience.helpers.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@Deprecated
public class RemoteParameter extends StringParameter implements SessionLayerListener {
	private static final Logger logger = LoggerFactory.getLogger(RemoteParameter.class);
	
	// the firmware uses a specific syntax.  these are elements of that syntax
	private static final String CUE = "> ";
	private static final String NOCHECKSUM = "NOCHECKSUM ";
	private static final String BADCHECKSUM = "BADCHECKSUM ";
	private static final String BADLINENUM = "BADLINENUM ";
	private static final String NOLINENUM = "NOLINENUM ";
	private static final String NEWLINE = "\n";
	private static final String COMMENT_START = ";";

	/**
	 * A command with a line number.
	 */
	public static class NumberedCommand {
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

	private transient SessionLayer sessionLayer;
	
	private transient int lastNumberAdded=0;
	private transient int nextNumberToSend=0;
	private transient boolean waitingForCue = false;
	// data being sent
	private final transient LinkedList<NumberedCommand> commands = new LinkedList<>();
	// data received
	private final transient ArrayList<Byte> partialMessage = new ArrayList<>();

	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		commands.clear();
		partialMessage.clear();
	}
	
	public RemoteParameter() {
		super("Connected to",null);
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
		if(sessionLayer !=null) {
			sessionLayer.closeConnection();
			sessionLayer =null;
		}
		resetCommands();
		partialMessage.clear();
	}
	
	public void openConnection() {
		sessionLayer = SessionLayerManager.requestNewSession(null);
		if(sessionLayer !=null) {
			sessionLayer.addListener(this);
			waitingForCue = true;
		}
	}

	public void update(double dt) {
		if(sessionLayer ==null) return;
		
		if(!waitingForCue && !commands.isEmpty()) {
			if(commands.get(commands.size()-1).n > nextNumberToSend) {
				sendMessageInternal(getCommand(nextNumberToSend++));
			}
		}
	}

	public boolean isConnectionOpen() {
		return sessionLayer !=null;
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
		commands.add(new NumberedCommand(lastNumberAdded, command));
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
			sessionLayer.sendMessage(command);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void reportDataSent(String msg) {
		//if(msg.contains("G0")) return;
		
		logger.info("RemoteEntity SEND " + msg.trim());
	}

	public void reportDataReceived(String msg) {
		if(msg.trim().isEmpty()) return;
		if(msg.contains("D17")) return;
		
		logger.info("RemoteEntity RECV " + msg.trim());
	}

	/**
	 * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
	 *
	 * @param src string to parse
	 * @return the portion of the string that is actually a number
	 */
	private String getNumberPortion(String src) {
		src = src.trim();
		int length = src.length();
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char character = src.charAt(i);
			if (Character.isDigit(character)) {
				result.append(character);
			}
		}
		return result.toString();
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
				logger.warn("NOCHECKSUM "+err);
			} catch (Exception ignored) {}

			return err;
		}
		if (line.lastIndexOf(BADCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				logger.warn("BADCHECKSUM "+x);
			} catch (Exception ignored) {}

			return err;
		}
		if (line.lastIndexOf(BADLINENUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADLINENUM) + BADLINENUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				logger.warn("BADLINENUM "+err);
			} catch (Exception ignored) {}

			return err;
		}
		if (line.lastIndexOf(NOLINENUM) != -1) {
			logger.warn("NOLINENUM");
			return 0;
		}

		return -1;
	}

	@Override
	public void networkSessionEvent(SessionLayerEvent evt) {
		if(evt.flag == SessionLayerEvent.DATA_AVAILABLE) receiveData((SessionLayer)evt.getSource(),(String)evt.data);
		else if(evt.flag == SessionLayerEvent.TRANSPORT_ERROR) transportError((SessionLayer)evt.getSource(),(String)evt.data);
	}
	
	private void receiveData(SessionLayer arg0, String data) {
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
				firePropertyChange(new PropertyChangeEvent(this, "data", null, data));
			}
		}
	}

	private void transportError(SessionLayer arg0, String errorMessage) {
		logger.error("RemoteEntity error: "+errorMessage);
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
