package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
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
	BlockingQueue<String> queue = new ArrayBlockingQueue<String>(256);
	
	public RemoteEntity() {
		super();
		setName("Connected to");
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
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

	public boolean isConnectionOpen() {
		return (connection!=null) && (connection.isOpen());
	}

	public void sendMessage(String command) {
		if(!isConnectionOpen()) return;
		
		try {
			connection.sendMessage(command);
		} catch (Exception e) {
			Log.error("RemoteEntity.sendMessage failed: "+e.getLocalizedMessage());
		}
	}
}
