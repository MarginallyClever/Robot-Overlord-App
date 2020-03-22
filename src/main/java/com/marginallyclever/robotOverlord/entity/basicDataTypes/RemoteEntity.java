package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class RemoteEntity extends StringEntity implements NetworkConnectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NetworkConnection connection;
	
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
		// we don't do anything, we just report on what the live robot says.
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
		// TODO Auto-generated method stub
		
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
