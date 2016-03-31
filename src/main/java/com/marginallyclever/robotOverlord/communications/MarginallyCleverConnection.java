package com.marginallyclever.robotOverlord.communications;


/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract interface MarginallyCleverConnection {
	void close();
	
	void open(String connectionName) throws Exception;

	void reconnect() throws Exception;

	boolean isConnectionOpen();

	String getRecentConnection();

	void sendMessage(String msg) throws Exception;
	
    public void addListener(MarginallyCleverConnectionReadyListener listener);
    
    public void removeListener(MarginallyCleverConnectionReadyListener listener);
}
