package com.marginallyclever.evilOverlord.communications;


/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract interface AbstractConnection {
	void close();
	
	void open(String connectionName) throws Exception;

	void reconnect() throws Exception;

	boolean isConnectionOpen();

	String getRecentConnection();

	void sendMessage(String msg) throws Exception;
	
    public void addListener(AbstractConnectionReadyListener listener);
    
    public void removeListener(AbstractConnectionReadyListener listener);
}
