package com.marginallyclever.communications;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class NetworkSession {
	// close this connection
	abstract public void closeConnection();

	// open a connection to a connection
	abstract public void openConnection(String connectionName) throws Exception;

	abstract public void reconnect() throws Exception;

	abstract public boolean isOpen();

	abstract public String getName();

	abstract public void sendMessage(String msg) throws Exception;
	
	abstract public TransportLayer getTransportLayer();

	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		listeners = new ArrayList<NetworkSessionListener>();
	}

	// OBSERVER PATTERN
	
	private transient ArrayList<NetworkSessionListener> listeners = new ArrayList<NetworkSessionListener>();
	
	public void addListener(NetworkSessionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(NetworkSessionListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(NetworkSessionEvent evt) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
				for (NetworkSessionListener a : listeners) {
					a.networkSessionEvent(evt);
				}
            }
		});
	}
}
