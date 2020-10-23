package com.marginallyclever.communications;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class NetworkSession {
	// Listeners which should be notified of a change to the percentage.
	private transient ArrayList<NetworkSessionListener> listeners;
	
	protected NetworkSession() {
		listeners = new ArrayList<NetworkSessionListener>();
	}

	protected void finalize() throws Throwable {
		listeners.clear();
		super.finalize();
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


	public void addListener(NetworkSessionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(NetworkSessionListener listener) {
		listeners.remove(listener);
	}

	public void notifyTransportError(String error) {
		for (NetworkSessionListener listener : listeners) {
			listener.transportError(this,error);
		}
	}

	public void notifySendBufferEmpty() {
		for (NetworkSessionListener listener : listeners) {
			listener.sendBufferEmpty(this);
		}
	}

	// tell all listeners data has arrived
	public void notifyDataAvailable(String line) {
		for (NetworkSessionListener listener : listeners) {
			listener.dataAvailable(this,line);
		}
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		listeners = new ArrayList<NetworkSessionListener>();
	}
}
