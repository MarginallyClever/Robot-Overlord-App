package com.marginallyclever.communications;

import java.util.ArrayList;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class NetworkConnection {
	// Listeners which should be notified of a change to the percentage.
	private ArrayList<NetworkConnectionListener> listeners;
	
	protected NetworkConnection() {
		listeners = new ArrayList<NetworkConnectionListener>();
	}
	
	// close this connection
	abstract public void closeConnection();

	// open a connection to a connection
	abstract public void openConnection(String connectionName) throws Exception;

	abstract public void reconnect() throws Exception;

	abstract public boolean isOpen();

	abstract public String getRecentConnection();

	abstract public void sendMessage(String msg) throws Exception;
	
	abstract public TransportLayer getTransportLayer();


	public void addListener(NetworkConnectionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(NetworkConnectionListener listener) {
		listeners.remove(listener);
	}

	public void notifyLineError(int lineNumber) {
		for (NetworkConnectionListener listener : listeners) {
			listener.lineError(this,lineNumber);
		}
	}

	public void notifySendBufferEmpty() {
		for (NetworkConnectionListener listener : listeners) {
			listener.sendBufferEmpty(this);
		}
	}

	// tell all listeners data has arrived
	public void notifyDataAvailable(String line) {
		for (NetworkConnectionListener listener : listeners) {
			listener.dataAvailable(this,line);
		}
	}
}
