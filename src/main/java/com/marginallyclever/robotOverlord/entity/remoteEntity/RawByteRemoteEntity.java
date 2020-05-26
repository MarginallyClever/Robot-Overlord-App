package com.marginallyclever.robotOverlord.entity.remoteEntity;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
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
public class RawByteRemoteEntity extends OneLineAtATimeRemoteEntity implements NetworkConnectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NetworkConnection connection;
	
	
	public RawByteRemoteEntity() {
		super();
		setName("Connected to");
	}
	
	public void closeConnection() {
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
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
	public void lineError(NetworkConnection arg0, int lineNumber) {}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {}

	@Override
	public void dataAvailable(NetworkConnection arg0, String data) {
		setChanged();
		notifyObservers(data);
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
		if(command.isEmpty()) return;
		
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
