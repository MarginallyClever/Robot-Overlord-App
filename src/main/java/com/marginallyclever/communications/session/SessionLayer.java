package com.marginallyclever.communications.session;

import com.marginallyclever.communications.transport.TransportLayer;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract class SessionLayer {
	// close this connection
	abstract public void closeConnection();

	// open a connection to a connection
	abstract public void openConnection(String connectionName) throws Exception;

	abstract public void reconnect() throws Exception;

	abstract public boolean isOpen();

	abstract public String getName();

	abstract public void sendMessage(String msg) throws Exception;
	
	abstract public TransportLayer getTransportLayer();


	// OBSERVER PATTERN
	
	private final transient ArrayList<SessionLayerListener> listeners = new ArrayList<>();
	
	public void addListener(SessionLayerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SessionLayerListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(SessionLayerEvent evt) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
				for (SessionLayerListener a : listeners) {
					a.networkSessionEvent(evt);
				}
            }
		});
	}
}
